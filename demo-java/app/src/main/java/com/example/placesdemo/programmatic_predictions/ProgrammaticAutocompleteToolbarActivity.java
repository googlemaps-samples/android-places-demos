// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.placesdemo.programmatic_predictions;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.ViewAnimator;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.placesdemo.MainActivity;
import com.example.placesdemo.R;
import com.example.placesdemo.model.GeocodingResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.LocationBias;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * An Activity that demonstrates programmatic as-you-type place predictions. The parameters of the
 * request are currently hard coded in this Activity, to modify these parameters (e.g. location
 * bias, place types, etc.), see {@link ProgrammaticAutocompleteToolbarActivity#getPlacePredictions(String)}.
 */
public class ProgrammaticAutocompleteToolbarActivity extends AppCompatActivity {

    private static final String TAG = ProgrammaticAutocompleteToolbarActivity.class.getSimpleName();
    private Handler handler = new Handler();
    private PlacePredictionAdapter adapter = new PlacePredictionAdapter();
    private Gson gson = new GsonBuilder().registerTypeAdapter(LatLng.class, new LatLngAdapter())
        .create();

    private RequestQueue queue;
    private PlacesClient placesClient;
    private AutocompleteSessionToken sessionToken;

    private ViewAnimator viewAnimator;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Use whatever theme was set from the MainActivity - some of these colors (e.g primary color)
        // will get picked up by the AutocompleteActivity.
        int theme = getIntent().getIntExtra(MainActivity.THEME_RES_ID_EXTRA, 0);
        if (theme != 0) {
            setTheme(theme);
        }
        setContentView(R.layout.activity_programmatic_autocomplete);
        setSupportActionBar(findViewById(R.id.toolbar));

        // Initialize members
        progressBar = findViewById(R.id.progress_bar);
        viewAnimator = findViewById(R.id.view_animator);
        placesClient = Places.createClient(this);
        queue = Volley.newRequestQueue(this);
        initRecyclerView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        final SearchView searchView =
            (SearchView) menu.findItem(R.id.search).getActionView();
        initSearchView(searchView);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.search) {
            sessionToken = AutocompleteSessionToken.newInstance();
            return false;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initSearchView(SearchView searchView) {
        searchView.setQueryHint(getString(R.string.search_a_place));
        searchView.setIconifiedByDefault(false);
        searchView.setFocusable(true);
        searchView.setIconified(false);
        searchView.requestFocusFromTouch();
        searchView.setOnQueryTextListener(new OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                progressBar.setIndeterminate(true);

                // Cancel any previous place prediction requests
                handler.removeCallbacksAndMessages(null);

                // Start a new place prediction request in 300 ms
                handler.postDelayed(() -> {
                    getPlacePredictions(newText);
                }, 300);
                return true;
            }
        });
    }

    private void initRecyclerView() {
        final RecyclerView recyclerView = findViewById(R.id.recycler_view);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView
            .addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));
        adapter.setPlaceClickListener(this::geocodePlaceAndDisplay);
    }

    /**
     * This method demonstrates the programmatic approach to getting place predictions. The
     * parameters in this request are currently biased to Kolkata, India.
     *
     * @param query the plus code query string (e.g. "GCG2+3M K")
     */
    private void getPlacePredictions(String query) {

        // The value of 'bias' biases prediction results to the rectangular region provided
        // (currently Kolkata). Modify these values to get results for another area.
        final LocationBias bias = RectangularBounds.newInstance(
            new LatLng(22.458744, 88.208162), // SW lat, lng
            new LatLng(22.730671, 88.524896) // NE lat, lng
        );

        // Create a new programmatic Place Autocomplete request in Places SDK for Android
        final FindAutocompletePredictionsRequest newRequest = FindAutocompletePredictionsRequest
            .builder()
            .setSessionToken(sessionToken)
            .setLocationBias(bias)
            .setTypeFilter(TypeFilter.ESTABLISHMENT)
            .setQuery(query)
            .setCountries("IN")
            .build();

        // Perform autocomplete predictions request
        placesClient.findAutocompletePredictions(newRequest).addOnSuccessListener((response) -> {
            List<AutocompletePrediction> predictions = response.getAutocompletePredictions();
            adapter.setPredictions(predictions);

            progressBar.setIndeterminate(false);
            viewAnimator.setDisplayedChild(predictions.isEmpty() ? 0 : 1);
        }).addOnFailureListener((exception) -> {
            progressBar.setIndeterminate(false);
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Place not found: " + apiException.getStatusCode());
            }
        });
    }

    /**
     * Performs a Geocode API request and displays the result in a dialog
     */
    private void geocodePlaceAndDisplay(AutocompletePrediction placePrediction) {
        // Construct the request URL
        final String apiKey = getString(R.string.places_api_key);
        final String url = "https://maps.googleapis.com/maps/api/geocode/json?place_id=%s&key=%s";
        final String requestURL = String.format(url, placePrediction.getPlaceId(), apiKey);

        // Use the HTTP request URL for Geocoding API to get geographic coordinates for the place
        JsonObjectRequest request = new JsonObjectRequest(Method.GET, requestURL, null,
            response -> {
                try {
                    // Inspect the value of "results" and make sure it's not empty
                    JSONArray results = response.getJSONArray("results");
                    if (results.length() == 0) {
                        Log.w(TAG, "No results from geocoding request.");
                        return;
                    }

                    // Use Gson to convert the response JSON object to a POJO
                    GeocodingResult result = gson.fromJson(
                        results.getString(0), GeocodingResult.class);
                    displayDialog(placePrediction, result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, error -> Log.e(TAG, "Request failed"));

        // Add the request to the Request queue.
        queue.add(request);
    }

    private void displayDialog(AutocompletePrediction place, GeocodingResult result) {
        new AlertDialog.Builder(this)
            .setTitle(place.getPrimaryText(null))
            .setMessage("Geocoding result:\n" + result.geometry.location)
            .setPositiveButton(android.R.string.ok, null)
            .show();
    }
}

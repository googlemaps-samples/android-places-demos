package com.example.placesdemo.programmatic_predictions;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.ViewAnimator;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.placesdemo.MainActivity;
import com.example.placesdemo.R;
import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import java.util.List;

/**
 * An Activity that demonstrates programmatic as-you-type place predictions.
 */
public class ProgrammaticAutocompleteToolbarActivity extends AppCompatActivity {

    private static final String TAG = ProgrammaticAutocompleteToolbarActivity.class.getSimpleName();
    private Handler handler = new Handler();
    private PlacePredictionAdapter adapter = new PlacePredictionAdapter();
    private PlacesClient placesClient;

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
    }

    /**
     * This method demonstrates the programmatic approach to getting place predictions
     *
     * @param query the plus code query string (e.g. "MC2W+RG K")
     */
    private void getPlacePredictions(String query) {
        // Create a new programmatic Place Autocomplete request in Places SDK for Android
        final FindAutocompletePredictionsRequest newRequest = FindAutocompletePredictionsRequest
            .builder()
            .setTypeFilter(TypeFilter.ESTABLISHMENT)
            .setQuery(query)
            .setCountries("US")
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
}

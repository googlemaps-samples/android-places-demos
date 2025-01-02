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

package com.example.placesdemo.programmatic_autocomplete

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.ViewAnimator
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.placesdemo.BuildConfig
import com.example.placesdemo.R
import com.example.placesdemo.model.GeocodingResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.LocationBias
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONException

/**
 * An Activity that demonstrates programmatic as-you-type place predictions.
 * The parameters of the request are currently hard coded in this Activity.
 * To modify these parameters (e.g. location bias, place types, etc.),
 * see [ProgrammaticAutocompleteGeocodingActivity.getPlacePredictions].
 *
 * @see <a href="https://developers.google.com/places/android-sdk/autocomplete#get_place_predictions_programmatically">Get Place Predictions Programmatically</a>
 */
class ProgrammaticAutocompleteGeocodingActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private val adapter = PlacePredictionAdapter()
    private val gson = GsonBuilder().registerTypeAdapter(LatLng::class.java, LatLngAdapter()).create()

    private lateinit var queue: RequestQueue
    private lateinit var placesClient: PlacesClient
    private var sessionToken: AutocompleteSessionToken? = null

    private lateinit var viewAnimator: ViewAnimator
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_programmatic_autocomplete)
        setSupportActionBar(findViewById(R.id.toolbar))

        // Initialize members
        progressBar = findViewById(R.id.progress_bar)
        viewAnimator = findViewById(R.id.view_animator)
        placesClient = Places.createClient(this)
        queue = Volley.newRequestQueue(this)
        initRecyclerView()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        val searchView = menu.findItem(R.id.search).actionView as SearchView
        initSearchView(searchView)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.search) {
            sessionToken = AutocompleteSessionToken.newInstance()
            return false
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initSearchView(searchView: SearchView) {
        searchView.queryHint = getString(R.string.search_a_place)
        searchView.isIconifiedByDefault = false
        searchView.isFocusable = true
        searchView.isIconified = false
        searchView.requestFocusFromTouch()
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                progressBar.isIndeterminate = true

                // Cancel any previous place prediction requests
                handler.removeCallbacksAndMessages(null)

                // Start a new place prediction request in 300 ms
                handler.postDelayed({ getPlacePredictions(newText) }, 300)
                return true
            }
        })
    }

    private fun initRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration(this, layoutManager.orientation))
        // Get just the location of the place using the Geocoding API
        adapter.onPlaceClickListener = { geocodePlaceAndDisplay(it) }
        // Alternative: Get more details about the place using Place Details
        // See https://goo.gle/paaln for help choosing between Geocoding and Place Details
        // adapter.onPlaceClickListener = { fetchPlaceAndDisplay(it) }
    }

    /**
     * This method demonstrates the programmatic approach to getting place predictions. The
     * parameters in this request are currently biased to Kolkata, India.
     *
     * @param query the plus code query string (e.g. "GCG2+3M K")
     */
    private fun getPlacePredictions(query: String) {
        // The value of 'bias' biases prediction results to the rectangular region provided
        // (currently Kolkata). Modify these values to get results for another area. Make sure to
        // pass in the appropriate value/s for .setCountries() in the
        // FindAutocompletePredictionsRequest.Builder object as well.
        val bias: LocationBias = RectangularBounds.newInstance(
            LatLng(22.458744, 88.208162),  // SW lat, lng
            LatLng(22.730671, 88.524896) // NE lat, lng
        )

        // Create a new programmatic Place Autocomplete request in Places SDK for Android
        val newRequest = FindAutocompletePredictionsRequest.builder()
            .setLocationBias(bias)
            .setCountries("IN")
            .setTypesFilter(listOf(PlaceTypes.ESTABLISHMENT))
            // Session Token only used to link related Place Details call. See https://goo.gle/paaln
            .setSessionToken(sessionToken)
            .setQuery(query)
            .build()

        // Perform autocomplete predictions request
        placesClient.findAutocompletePredictions(newRequest)
            .addOnSuccessListener { response ->
                val predictions = response.autocompletePredictions
                adapter.setPredictions(predictions)
                progressBar.isIndeterminate = false
                viewAnimator.displayedChild = if (predictions.isEmpty()) 0 else 1
            }.addOnFailureListener { exception: Exception? ->
                progressBar.isIndeterminate = false
                if (exception is ApiException) {
                    Log.e(TAG, "Place not found: ${exception.message}")
                }
            }
    }

    /**
     * Performs a Geocoding API request and displays the result in a dialog.
     * Be sure to enable the Geocoding API in your project and set API key restrictions.
     *
     * @see <a href="https://developers.google.com/maps/documentation/geocoding/overview">Geocoding API</a>
     */
    private fun geocodePlaceAndDisplay(placePrediction: AutocompletePrediction) {
        // Construct the request URL
        val apiKey = BuildConfig.PLACES_API_KEY
        val requestURL =
            "https://maps.googleapis.com/maps/api/geocode/json?place_id=${placePrediction.placeId}&key=$apiKey"

        // Use the HTTP request URL for Geocoding API to get geographic coordinates for the place
        val request = JsonObjectRequest(Request.Method.GET, requestURL, null, { response ->
            try {
                val status: String = response.getString("status")
                if (status != "OK") {
                    Log.e(TAG, "$status " + response.getString("error_message"))
                }

                // Inspect the value of "results" and make sure it's not empty
                val results: JSONArray = response.getJSONArray("results")
                if (results.length() == 0) {
                    Log.w(TAG, "No results from geocoding request.")
                    return@JsonObjectRequest
                }

                // Use Gson to convert the response JSON object to a POJO
                val result: GeocodingResult = gson.fromJson(results.getString(0), GeocodingResult::class.java)
                displayDialog(placePrediction, result)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }, { error ->
            Log.e(TAG, "Request failed", error)
        })

        // Add the request to the Request queue.
        queue.add(request)
    }

    private fun displayDialog(place: AutocompletePrediction, result: GeocodingResult) {
        AlertDialog.Builder(this)
            .setTitle(place.getPrimaryText(null))
            .setMessage("Geocoding result:\n" + result.geometry?.location)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    /**
     * Performs a Place Details request and displays the result in a dialog.
     *
     * @see <a href="https://developers.google.com/maps/documentation/places/android-sdk/place-details">Place Details</a>
     */
    private fun fetchPlaceAndDisplay(placePrediction: AutocompletePrediction) {
        // Specify the fields to return.
        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS)

        // Construct a request object, passing the place ID and fields array.
        val request = FetchPlaceRequest.newInstance(placePrediction.placeId, placeFields)

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response: FetchPlaceResponse ->
                val place = response.place
                AlertDialog.Builder(this)
                    .setTitle(place.name)
                    .setMessage("located at:\n" + place.address)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
                Log.i(TAG, "Place found: ${place.name}")
            }.addOnFailureListener { exception: Exception ->
                if (exception is ApiException) {
                    Log.e(TAG, "Place not found: ${exception.message} ${exception.statusCode}")
                }
            }
    }

    companion object {
        private val TAG = ProgrammaticAutocompleteGeocodingActivity::class.java.simpleName
    }
}


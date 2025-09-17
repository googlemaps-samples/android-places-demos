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

import com.google.android.material.search.SearchView
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.placesdemo.BaseActivity
import com.example.placesdemo.BuildConfig
import com.example.placesdemo.R
import com.example.placesdemo.databinding.ActivityProgrammaticAutocompleteBinding
import com.example.placesdemo.model.GeocodingResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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

private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

/**
 * Extension function to get a color from the current theme using its attribute resource ID.
 */
@ColorInt
fun Context.getColorFromTheme(@AttrRes colorAttributeResId: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(colorAttributeResId, typedValue, true)
    return typedValue.data
}

/**
 * An activity that demonstrates programmatic as-you-type place predictions.
 *
 * @see https://developers.google.com/maps/documentation/places/android-sdk/autocomplete#get_place_predictions_programmatically
 */
class ProgrammaticAutocompleteGeocodingActivity : BaseActivity() {

    // A handler for delaying place prediction requests.
    private val handler = Handler(Looper.getMainLooper())
    private val adapter = PlacePredictionAdapter()
    private val gson =
        GsonBuilder().registerTypeAdapter(LatLng::class.java, LatLngAdapter()).create()

    private lateinit var queue: RequestQueue
    private lateinit var placesClient: PlacesClient
    private var sessionToken: AutocompleteSessionToken? = null
    private lateinit var binding: ActivityProgrammaticAutocompleteBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private var lastKnownLocation: Location? = null
    private var locationPermissionGranted = false
    private val defaultLocation = LatLng(40.0150, -105.2705) // Boulder, Colorado

    private var colorOnPrimary: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgrammaticAutocompleteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // In your Fragment's onViewCreated or Activity's onCreate
        val searchBar = binding.searchBar // view.findViewById<SearchBar>(R.id.search_bar)
        val searchView = binding.searchView // view.findViewById<SearchView>(R.id.search_view)

        // This is the critical line that makes it work!
        searchView.setupWithSearchBar(searchBar)

        // Now you can initialize your SearchView listeners
        initSearchView(searchView)

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Prompt the user for permission.
        getLocationPermission()
        
        // Initialize members
        placesClient = Places.createClient(this)
        queue = Volley.newRequestQueue(this)
        initRecyclerView()

        colorOnPrimary = this.getColorFromTheme(com.google.android.material.R.attr.colorOnPrimary)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        val searchView =
            menu.findItem(R.id.search).actionView as SearchView
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
        // This listener will be invoked when the user types in the search bar.
        searchView.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed here
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()

                // Cancel any previous place prediction requests
                handler.removeCallbacksAndMessages(null)


                // This is a common practice to avoid making too many requests while the user is typing.
                handler.postDelayed({
                        if (query.isNotEmpty()) binding.progressBar.visibility = View.VISIBLE
                        getPlacePredictions(query)
                    },
                    300
                )
            }

            override fun afterTextChanged(s: Editable?) {
                // No action needed here
            }
        })
    }

    private fun initRecyclerView() {
        val recyclerView = binding.placeSearchResultsView
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
     * Fetches place predictions from the Places API.
     *
     * @param query The search query.
     *
     * @see https://developers.google.com/maps/documentation/places/android-sdk/autocomplete#get_place_predictions_programmatically
     */
    private fun getPlacePredictions(query: String) {
        val latLng = LatLng(lastKnownLocation?.latitude ?: defaultLocation.latitude, lastKnownLocation?.longitude ?: defaultLocation.longitude)
        val bias: LocationBias = RectangularBounds.newInstance(
            LatLng(latLng.latitude - RADIUS_DEGREES, latLng.longitude - RADIUS_DEGREES),
            LatLng(latLng.latitude + RADIUS_DEGREES, latLng.longitude + RADIUS_DEGREES)
        )

        // Create a new programmatic Place Autocomplete request in Places SDK for Android
        val newRequest = FindAutocompletePredictionsRequest.builder()
            .setOrigin(latLng)
            // A location bias is a soft restriction that can be used to filter results to a specific area.
            .setLocationBias(bias)
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
                binding.progressBar.visibility = View.INVISIBLE
                binding.resultsViewAnimator.displayedChild =
                    if (predictions.isEmpty() && query.isNotEmpty()) 1 else 0
            }.addOnFailureListener { exception: Exception? ->
                binding.progressBar.visibility = View.INVISIBLE
                if (exception is ApiException) {
                    Log.e(TAG, "Place not found: ${exception.message}")
                }
            }
    }

    /**
     * Fetches the geographic coordinates for a place and displays the result in a dialog.
     *
     * @param placePrediction The place prediction to geocode.
     *
     * @see https://developers.google.com/maps/documentation/geocoding/overview
     */
    private fun geocodePlaceAndDisplay(placePrediction: AutocompletePrediction) {
        // Construct the request URL
        val apiKey = BuildConfig.PLACES_API_KEY
        val requestURL =
            "https://maps.googleapis.com/maps/api/geocode/json?place_id=${placePrediction.placeId}&key=$apiKey"

        // The Places SDK for Android does not support geocoding.
        // This sample uses Volley to make a request to the Geocoding API.
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
                val result: GeocodingResult =
                    gson.fromJson(results.getString(0), GeocodingResult::class.java)
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
     * Fetches details for a place and displays the result in a dialog.
     *
     * @param placePrediction The prediction to fetch details for.
     *
     * @see https://developers.google.com/maps/documentation/places/android-sdk/place-details
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
                    .setTitle(place.displayName)
                    .setMessage("located at:\n" + place.formattedAddress)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
                Log.i(TAG, "Place found: ${place.displayName}")
            }.addOnFailureListener { exception: Exception ->
                if (exception is ApiException) {
                }
            }
    }

    /**
     * Gets the last known location of the device.
     */
    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            Log.d(TAG, "Last known location: " + lastKnownLocation!!.latitude + ", " + lastKnownLocation!!.longitude)
                        } else {
                            Log.d(TAG, "Last known location is null. Using defaults.")
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
            getDeviceLocation()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true
                    getDeviceLocation()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }



    companion object {
        private val TAG = ProgrammaticAutocompleteGeocodingActivity::class.java.simpleName
        private const val RADIUS_DEGREES = 0.001
    }
}
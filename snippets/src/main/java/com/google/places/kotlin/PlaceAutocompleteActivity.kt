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

package com.google.places.kotlin

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.places.R
import com.google.places.databinding.ActivityPlaceAutocompleteBinding

class PlaceAutocompleteActivity : AppCompatActivity() {
    private lateinit var placesClient: PlacesClient
    private lateinit var binding: ActivityPlaceAutocompleteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaceAutocompleteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "$title (Kotlin)"

        WindowCompat.setDecorFitsSystemWindows(window, false)

        placesClient = (application as MainApplication).getPlacesClient()

        binding.useRestrictionSwitch.setOnCheckedChangeListener { _, _ ->
            initAutocompleteSupportFragment()
        }
        initAutocompleteSupportFragment()
        binding.autocompleteIntentButton.setOnClickListener { startAutocompleteIntent() }
        binding.programmaticAutocompleteButton.setOnClickListener {
            programmaticPlacePredictions(binding.autocompleteQuery.text.toString())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initAutocompleteSupportFragment() {
        // [START maps_places_autocomplete_support_fragment]
        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.DISPLAY_NAME))

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                binding.autocompleteResult.text = getString(
                    R.string.place_selection,
                    place.displayName,
                    place.id,
                    place.formattedAddress
                )
                Log.i(TAG, "Place: ${place.displayName}, ${place.id}")
            }

            override fun onError(status: Status) {
                binding.autocompleteResult.text = getString(R.string.an_error_occurred, status)
                Log.i(TAG, "An error occurred: $status")
            }
        })
        // [END maps_places_autocomplete_support_fragment]

        val bounds = RectangularBounds.newInstance(
            LatLng(-33.880490, 151.184363),
            LatLng(-33.858754, 151.229596)
        )

        // Clear the previous restriction or bias
        autocompleteFragment.setLocationRestriction(null)
        autocompleteFragment.setLocationBias(null)

        if (binding.useRestrictionSwitch.isChecked) {
            // [START maps_places_autocomplete_location_restriction]
            autocompleteFragment.setLocationRestriction(bounds)
            // [END maps_places_autocomplete_location_restriction]
        } else {
            // [START maps_places_autocomplete_location_bias]
            autocompleteFragment.setLocationBias(bounds)
            // [END maps_places_autocomplete_location_bias]
        }

        // [START maps_places_autocomplete_type_filter]
        autocompleteFragment.setTypesFilter(listOf(PlaceTypes.ADDRESS))
        // [END maps_places_autocomplete_type_filter]

        // [START maps_places_autocomplete_type_filter_multiple]
        autocompleteFragment.setTypesFilter(listOf("landmark", "restaurant", "store"))
        // [END maps_places_autocomplete_type_filter_multiple]


        // [START maps_places_autocomplete_country_filter]
        autocompleteFragment.setCountries("AU", "NZ")
        // [END maps_places_autocomplete_country_filter]
    }

    // [START maps_places_autocomplete_intent]

    // [START_EXCLUDE silent]
    private fun startAutocompleteIntent() {
        // [END_EXCLUDE]
        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        val fields = listOf(Place.Field.ID, Place.Field.DISPLAY_NAME, Place.Field.FORMATTED_ADDRESS)

        // [START maps_places_intent_type_filter]
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .setTypesFilter(listOf(PlaceTypes.ESTABLISHMENT))
            .build(this)
        // [END maps_places_intent_type_filter]

        startAutocomplete.launch(intent)
        // [END maps_places_autocomplete_intent]
    }

    // [START maps_places_on_activity_result]
    private val startAutocomplete =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val intent = result.data
                if (intent != null) {
                    val place = Autocomplete.getPlaceFromIntent(intent)
                    binding.autocompleteResult.text = getString(
                        R.string.place_selection,
                        place.displayName,
                        place.id,
                        place.formattedAddress)
                    Log.i(
                        TAG, "Place: ${place.displayName}, ${place.id}"
                    )
                }
            } else if (result.resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                binding.autocompleteResult.setText(R.string.user_canceled_autocomplete)
                Log.i(TAG, "User canceled autocomplete")
            }
        }
    // [END maps_places_on_activity_result]

    private fun programmaticPlacePredictions(query: String) {
        // [START maps_places_programmatic_place_predictions]
        // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
        // and once again when the user makes a selection (for example when calling fetchPlace()).
        val token = AutocompleteSessionToken.newInstance()

        // Create a RectangularBounds object.
        val bounds = RectangularBounds.newInstance(
            LatLng(-33.880490, 151.184363),
            LatLng(-33.858754, 151.229596)
        )
        // Use the builder to create a FindAutocompletePredictionsRequest.
        val request =
            FindAutocompletePredictionsRequest.builder()
                // Call either setLocationBias() OR setLocationRestriction().
                .setLocationBias(bounds)
                //.setLocationRestriction(bounds)
                .setOrigin(LatLng(-33.8749937, 151.2041382))
                .setCountries("AU", "NZ")
                .setTypesFilter(listOf(PlaceTypes.ESTABLISHMENT))
                .setSessionToken(token)
                .setQuery(query)
                .build()
        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                val builder = StringBuilder()
                for (prediction in response.autocompletePredictions) {
                    builder.append(prediction.getPrimaryText(null).toString()).append("\n")
                    Log.i(TAG, prediction.placeId)
                    Log.i(TAG, prediction.getPrimaryText(null).toString())
                }
                binding.autocompleteResult.text = builder.toString()
            }.addOnFailureListener { exception: Exception? ->
                if (exception is ApiException) {
                    Log.e(TAG, "Place not found: ${exception.statusCode}")
                    binding.autocompleteResult.text = getString(R.string.place_not_found, exception.message)
                }
            }
        // [END maps_places_programmatic_place_predictions]
    }

    companion object {
        val TAG = PlaceAutocompleteActivity::class.java.simpleName
    }
}
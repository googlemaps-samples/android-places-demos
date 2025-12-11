/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.placesdemo

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.example.placesdemo.databinding.PlaceAutocompleteActivityBinding
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.LocationBias
import com.google.android.libraries.places.api.model.LocationRestriction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.PlaceAutocompleteActivity
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.PlaceAutocomplete
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode

/**
 * Activity to demonstrate Place Autocomplete (activity widget intent, fragment widget, and
 * [PlacesClient.findAutocompletePredictions]).
 */
class PlaceAutocompleteActivity : BaseActivity() {

    private lateinit var placesClient: PlacesClient
    private lateinit var fieldSelector: FieldSelector

    private lateinit var binding: PlaceAutocompleteActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = PlaceAutocompleteActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.topBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Retrieve a PlacesClient (previously initialized - see MainActivity)
        placesClient = Places.createClient(this)

        // Set up view objects

        val useTypesFilterCheckBox =
            findViewById<CheckBox>(R.id.autocomplete_use_types_filter_checkbox)
        useTypesFilterCheckBox.setOnCheckedChangeListener { _, isChecked: Boolean ->
            binding.autocompleteTypesFilterEdittext.isEnabled = isChecked
        }
        fieldSelector = FieldSelector(
            findViewById(R.id.use_custom_fields),
            findViewById(R.id.custom_fields_list),
            savedInstanceState
        )
        setupAutocompleteSupportFragment()

        // Set listeners for Autocomplete activity
        binding.autocompleteActivityButton
            .setOnClickListener { startAutocompleteActivity() }

        // Set listeners for programmatic Autocomplete
        binding.fetchAutocompletePredictionsButton
            .setOnClickListener { findAutocompletePredictions() }

        // UI initialization
        setLoading(false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        fieldSelector.onSaveInstanceState(outState)
    }

    private fun setupAutocompleteSupportFragment() {
        val autocompleteSupportFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_support_fragment) as AutocompleteSupportFragment?
        autocompleteSupportFragment!!.setPlaceFields(placeFields)
        autocompleteSupportFragment.setOnPlaceSelectedListener(placeSelectionListener)
        findViewById<View>(R.id.autocomplete_support_fragment_update_button)
            .setOnClickListener {
                autocompleteSupportFragment
                    .setPlaceFields(placeFields)
                    .setText(query)
                    .setHint(hint)
                    .setCountries(countries)
                    .setLocationBias(locationBias)
                    .setLocationRestriction(locationRestriction)
                    .setTypesFilter(getTypesFilter())
                    .setActivityMode(mode)
            }
    }

    private val placeSelectionListener: PlaceSelectionListener
        get() = object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                binding.response.text =
                    StringUtil.stringifyAutocompleteWidget(place, isDisplayRawResultsChecked)
            }

            override fun onError(status: Status) {
                binding.response.text = status.statusMessage
            }
        }

    /**
     * Launches Autocomplete activity and handles result
     */
    private var autocompleteLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->
        when (result.resultCode) {
            RESULT_OK -> {
                val data: Intent? = result.data
                if (data != null) {
                    val place = PlaceAutocomplete.getPredictionFromIntent(data)
                    binding.response.text =
                        StringUtil.stringifyAutocompletePrediction(place, isDisplayRawResultsChecked)
                }
            }
            PlaceAutocompleteActivity.RESULT_ERROR -> {
                val status = PlaceAutocomplete.getResultStatusFromIntent(intent)
                binding.response.text = status?.statusMessage
            }
            RESULT_CANCELED -> {
                // The user canceled the operation.
            }
        }
    }

    private fun startAutocompleteActivity() {
        val autocompleteIntent = PlaceAutocomplete.IntentBuilder()
            .setInitialQuery(query)
            .setCountries(countries)
            .setLocationBias(locationBias)
            .setLocationRestriction(locationRestriction)
            .setTypesFilter(getTypesFilter())
            .build(this)
        autocompleteLauncher.launch(autocompleteIntent)
    }

    private fun findAutocompletePredictions() {
        setLoading(true)
        val requestBuilder = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setCountries(countries)
            .setOrigin(origin)
            .setLocationBias(locationBias)
            .setLocationRestriction(locationRestriction)
            .setTypesFilter(getTypesFilter())
        if (isUseSessionTokenChecked) {
            requestBuilder.sessionToken = AutocompleteSessionToken.newInstance()
        }
        val task = placesClient.findAutocompletePredictions(requestBuilder.build())
        task.addOnSuccessListener { response: FindAutocompletePredictionsResponse? ->
            response?.let {
                binding.response.text = StringUtil.stringify(it, isDisplayRawResultsChecked)
            }
        }
        task.addOnFailureListener { exception: Exception ->
            exception.printStackTrace()
            binding.response.text = exception.message
        }
        task.addOnCompleteListener {
            setLoading(
                false
            )
        }
    }

    //////////////////////////
    // Helper methods below //
    //////////////////////////
    private val placeFields: List<Place.Field>
        get() = if ((findViewById<View>(R.id.use_custom_fields) as CheckBox).isChecked) {
            fieldSelector.selectedFields
        } else {
            fieldSelector.allFields
        }

    private val query: String?
        get() = getTextViewValue(R.id.autocomplete_query)

    private val hint: String?
        get() = getTextViewValue(R.id.autocomplete_hint)

    private val countries: List<String>
        get() {
            val countryString = getTextViewValue(R.id.autocomplete_country) ?: return emptyList()
            return StringUtil.countriesStringToArrayList(countryString)
        }

    private fun getTextViewValue(@IdRes textViewResId: Int): String? {
        val value = (findViewById<View>(textViewResId) as TextView).text.toString()
        return if (TextUtils.isEmpty(value)) null else value
    }

    private val locationBias: LocationBias?
        get() = getBounds(
            R.id.autocomplete_location_bias_south_west, R.id.autocomplete_location_bias_north_east
        )

    private val locationRestriction: LocationRestriction?
        get() = getBounds(
            R.id.autocomplete_location_restriction_south_west,
            R.id.autocomplete_location_restriction_north_east
        )

    private fun getBounds(resIdSouthWest: Int, resIdNorthEast: Int): RectangularBounds? {
        val southWest = findViewById<TextView>(resIdSouthWest).text.toString()
        val northEast = findViewById<TextView>(resIdNorthEast).text.toString()
        if (TextUtils.isEmpty(southWest) && TextUtils.isEmpty(northEast)) {
            return null
        }
        val bounds = StringUtil.convertToLatLngBounds(southWest, northEast)
        if (bounds == null) {
            showErrorAlert(R.string.error_alert_message_invalid_bounds)
            return null
        }
        return RectangularBounds.newInstance(bounds)
    }

    private val origin: LatLng?
        get() {
            val originStr =
                findViewById<TextView>(R.id.autocomplete_location_origin).text.toString()
            if (TextUtils.isEmpty(originStr)) {
                return null
            }
            val origin = StringUtil.convertToLatLng(originStr)
            if (origin == null) {
                showErrorAlert(R.string.error_alert_message_invalid_origin)
                return null
            }
            return origin
        }

    private fun getTypesFilter(): List<String> {
        return if (binding.autocompleteTypesFilterEdittext.isEnabled)
            binding.autocompleteTypesFilterEdittext.text.toString().split("[\\s,]+".toRegex())
        else emptyList()
    }

    // This Enum is deprecated, but there is no replacement. See https://developers.google.com/maps/documentation/places/android-sdk/reference/com/google/android/libraries/places/widget/model/AutocompleteActivityMode
    private val mode: AutocompleteActivityMode
        get() {
            val isOverlayMode =
                binding.autocompleteActivityOverlayMode.isChecked
            return if (isOverlayMode) AutocompleteActivityMode.OVERLAY else AutocompleteActivityMode.FULLSCREEN
        }

    private val isDisplayRawResultsChecked: Boolean
        get() = binding.displayRawResults.isChecked

    private val isUseSessionTokenChecked: Boolean
        get() = binding.autocompleteUseSessionToken.isChecked

    private fun setLoading(loading: Boolean) {
        findViewById<View>(R.id.loading).visibility = if (loading) View.VISIBLE else View.INVISIBLE
    }

    private fun showErrorAlert(@StringRes messageResId: Int) {
        AlertDialog.Builder(this)
            .setTitle(R.string.error_alert_title)
            .setMessage(messageResId)
            .show()
    }
}
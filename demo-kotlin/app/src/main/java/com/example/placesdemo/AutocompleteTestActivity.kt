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
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.LocationBias
import com.google.android.libraries.places.api.model.LocationRestriction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import java.util.*

/**
 * Activity for testing Autocomplete (activity and fragment widgets, and programmatic).
 */
class AutocompleteTestActivity : AppCompatActivity() {

    private lateinit var placesClient: PlacesClient
    private lateinit var responseView: TextView
    private lateinit var fieldSelector: FieldSelector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use whatever theme was set from the MainActivity - some of these colors (e.g primary color)
        // will get picked up by the AutocompleteActivity.
        val theme = intent.getIntExtra(MainActivity.THEME_RES_ID_EXTRA, 0)
        if (theme != 0) {
            setTheme(theme)
        }
        setContentView(R.layout.autocomplete_test_activity)

        // Retrieve a PlacesClient (previously initialized - see MainActivity)
        placesClient = Places.createClient(this)

        // Set up view objects
        responseView = findViewById(R.id.response)
        val typeFilterSpinner = findViewById<Spinner>(R.id.autocomplete_type_filter)
        typeFilterSpinner.adapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1, Arrays.asList(*TypeFilter.values()))
        val useTypeFilterCheckBox = findViewById<CheckBox>(R.id.autocomplete_use_type_filter)
        useTypeFilterCheckBox.setOnCheckedChangeListener { _, isChecked: Boolean -> typeFilterSpinner.isEnabled = isChecked }
        fieldSelector = FieldSelector(
            findViewById(R.id.use_custom_fields),
            findViewById(R.id.custom_fields_list),
            savedInstanceState)
        setupAutocompleteSupportFragment()

        // Set listeners for Autocomplete activity
        findViewById<View>(R.id.autocomplete_activity_button)
            .setOnClickListener { startAutocompleteActivity() }

        // Set listeners for programmatic Autocomplete
        findViewById<View>(R.id.fetch_autocomplete_predictions_button)
            .setOnClickListener { findAutocompletePredictions() }

        // UI initialization
        setLoading(false)
        typeFilterSpinner.isEnabled = false
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        fieldSelector.onSaveInstanceState(bundle)
    }

    private fun setupAutocompleteSupportFragment() {
        val autocompleteSupportFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_support_fragment) as AutocompleteSupportFragment?
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
                    .setTypeFilter(typeFilter)
                    .setActivityMode(mode)
            }
    }

    private val placeSelectionListener: PlaceSelectionListener
        get() = object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                responseView.text = StringUtil.stringifyAutocompleteWidget(place, isDisplayRawResultsChecked)
            }

            override fun onError(status: Status) {
                responseView.text = status.statusMessage
            }
        }

    /**
     * Called when AutocompleteActivity finishes
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE && intent != null) {
            when (resultCode) {
                AutocompleteActivity.RESULT_OK -> {
                    val place = Autocomplete.getPlaceFromIntent(intent)
                    responseView.text = StringUtil.stringifyAutocompleteWidget(place, isDisplayRawResultsChecked)
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val status = Autocomplete.getStatusFromIntent(intent)
                    responseView.text = status.statusMessage
                }
                AutocompleteActivity.RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
        }

        // Required because this class extends AppCompatActivity which extends FragmentActivity
        // which implements this method to pass onActivityResult calls to child fragments
        // (eg AutocompleteFragment).
        super.onActivityResult(requestCode, resultCode, intent)
    }

    private fun startAutocompleteActivity() {
        val autocompleteIntent = Autocomplete.IntentBuilder(mode, placeFields)
            .setInitialQuery(query)
            .setHint(hint)
            .setCountries(countries)
            .setLocationBias(locationBias)
            .setLocationRestriction(locationRestriction)
            .setTypeFilter(typeFilter)
            .build(this)
        startActivityForResult(autocompleteIntent, AUTOCOMPLETE_REQUEST_CODE)
    }

    private fun findAutocompletePredictions() {
        setLoading(true)
        val requestBuilder = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setCountries(countries)
            .setOrigin(origin)
            .setLocationBias(locationBias)
            .setLocationRestriction(locationRestriction)
            .setTypeFilter(typeFilter)
        if (isUseSessionTokenChecked) {
            requestBuilder.setSessionToken(AutocompleteSessionToken.newInstance())
        }
        val task = placesClient.findAutocompletePredictions(requestBuilder.build())
        task.addOnSuccessListener { response: FindAutocompletePredictionsResponse? ->
            response?.let {
                responseView.text = StringUtil.stringify(it, isDisplayRawResultsChecked)
            }
        }
        task.addOnFailureListener { exception: Exception ->
            exception.printStackTrace()
            responseView.text = exception.message
        }
        task.addOnCompleteListener { response: Task<FindAutocompletePredictionsResponse>? -> setLoading(false) }
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
            R.id.autocomplete_location_bias_south_west, R.id.autocomplete_location_bias_north_east)

    private val locationRestriction: LocationRestriction?
        get() = getBounds(
            R.id.autocomplete_location_restriction_south_west,
            R.id.autocomplete_location_restriction_north_east)

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
            val originStr = findViewById<TextView>(R.id.autocomplete_location_origin).text.toString()
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

    private val typeFilter: TypeFilter?
        get() {
            val typeFilter = findViewById<Spinner>(R.id.autocomplete_type_filter)
            return if (typeFilter.isEnabled) typeFilter.selectedItem as TypeFilter else null
        }

    private val mode: AutocompleteActivityMode
        get() {
            val isOverlayMode = findViewById<CheckBox>(R.id.autocomplete_activity_overlay_mode).isChecked
            return if (isOverlayMode) AutocompleteActivityMode.OVERLAY else AutocompleteActivityMode.FULLSCREEN
        }

    private val isDisplayRawResultsChecked: Boolean
        get() = findViewById<CheckBox>(R.id.display_raw_results).isChecked

    private val isUseSessionTokenChecked: Boolean
        get() = findViewById<CheckBox>(R.id.autocomplete_use_session_token).isChecked

    private fun setLoading(loading: Boolean) {
        findViewById<View>(R.id.loading).visibility = if (loading) View.VISIBLE else View.INVISIBLE
    }

    private fun showErrorAlert(@StringRes messageResId: Int) {
        AlertDialog.Builder(this)
            .setTitle(R.string.error_alert_title)
            .setMessage(messageResId)
            .show()
    }

    companion object {
        private const val AUTOCOMPLETE_REQUEST_CODE = 23487
    }
}
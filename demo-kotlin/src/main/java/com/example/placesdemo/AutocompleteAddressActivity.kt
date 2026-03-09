/*
 * Copyright 2022 Google LLC
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

import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources.NotFoundException
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewStub
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.placesdemo.databinding.AutocompleteAddressActivityBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.AddressComponents
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.maps.android.SphericalUtil.computeDistanceBetween
import androidx.core.view.isGone

/**
 *  Activity for using Place Autocomplete to assist filling out an address form.
 */
class AutocompleteAddressActivity : BaseActivity(),
    OnMapReadyCallback {
    private lateinit var mapPanel: View

    private var mapFragment: SupportMapFragment? = null
    private lateinit var coordinates: LatLng
    private var map: GoogleMap? = null
    private var marker: Marker? = null
    private var checkProximity = false
    private lateinit var binding: AutocompleteAddressActivityBinding
    private val acceptedProximity = 150.0
    private var startAutocompleteIntentListener = View.OnClickListener { view: View ->
        view.setOnClickListener(null)
        startAutocompleteIntent()
    }

    // [START maps_solutions_android_autocomplete_define]
    private val startAutocomplete = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback { result: ActivityResult ->
            binding.autocompleteAddress1.setOnClickListener(startAutocompleteIntentListener)
            if (result.resultCode == RESULT_OK) {
                val intent = result.data
                if (intent != null) {
                    val place = Autocomplete.getPlaceFromIntent(intent)

                    // Write a method to read the address components from the Place
                    // and populate the form with the address components
                    Log.d(TAG, "Place: " + place.addressComponents)
                    fillInAddress(place)
                }
            } else if (result.resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                Log.i(TAG, "User canceled autocomplete")
            }
        })
    // [END maps_solutions_android_autocomplete_define]

    // [START maps_solutions_android_autocomplete_intent]
    private fun startAutocompleteIntent() {
        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        val fields = listOf(
            Place.Field.ADDRESS_COMPONENTS,
            Place.Field.LOCATION, Place.Field.VIEWPORT
        )

        // Build the autocomplete intent with field, country, and type filters applied
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .setCountries(listOf("US"))
            .setTypesFilter(listOf(PlaceTypes.ADDRESS))
            .build(this)
        startAutocomplete.launch(intent)
    }
    // [END maps_solutions_android_autocomplete_intent]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = AutocompleteAddressActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.topBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Attach an Autocomplete intent to the Address 1 EditText field
        binding.autocompleteAddress1.setOnClickListener(startAutocompleteIntentListener)

        // Update checkProximity when user checks the checkbox
        val checkProximityBox = findViewById<CheckBox>(R.id.checkbox_proximity)
        checkProximityBox.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            // Set the boolean to match user preference for when the Submit button is clicked
            checkProximity = isChecked
        }

        // Submit and optionally check proximity
        val saveButton = findViewById<Button>(R.id.autocomplete_save_button)
        saveButton.setOnClickListener { saveForm() }

        // Reset the form
        val resetButton = findViewById<Button>(R.id.autocomplete_reset_button)
        resetButton.setOnClickListener { clearForm() }
    }

    private fun saveForm() {
        Log.d(TAG, "checkProximity = $checkProximity")
        if (checkProximity) {
            checkLocationPermissions()
        } else {
            Toast.makeText(this, R.string.autocomplete_skipped_message, Toast.LENGTH_SHORT).show()
        }
    }

    // [START maps_solutions_android_location_permissions]
    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            getAndCompareLocations()
        } else {
            requestPermissionLauncher.launch(
                permission.ACCESS_FINE_LOCATION
            )
        }
    }
    // [END maps_solutions_android_location_permissions]

    @SuppressLint("MissingPermission")
    private fun getAndCompareLocations() {
        // TODO: Detect and handle if user has entered or modified the address manually and update
        // the coordinates variable to the Lat/Lng of the manually entered address. May use
        // Geocoding API to convert the manually entered address to a Lat/Lng.
        val enteredLocation = coordinates
        map!!.isMyLocationEnabled = true

        // [START maps_solutions_android_location_get]
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation
            .addOnSuccessListener(this) { location: Location? ->
                // Got last known location. In some rare situations this can be null.
                if (location == null) {
                    return@addOnSuccessListener
                }
                val currentLocation = LatLng(location.latitude, location.longitude)
                // [START_EXCLUDE]
                Log.d(TAG, "device location = $currentLocation")
                Log.d(TAG, "entered location = $enteredLocation")

                // [START maps_solutions_android_location_distance]
                // Use the computeDistanceBetween function in the Maps SDK for Android Utility Library
                // to use spherical geometry to compute the distance between two Lat/Lng points.
                val distanceInMeters: Double =
                    computeDistanceBetween(currentLocation, enteredLocation)
                if (distanceInMeters <= acceptedProximity) {
                    Log.d(TAG, "location matched")
                    // TODO: Display UI based on the locations matching
                } else {
                    Log.d(TAG, "location not matched")
                    // TODO: Display UI based on the locations not matching
                }
                // [END maps_solutions_android_location_distance]
                // [END_EXCLUDE]
            }
    }
    // [END maps_solutions_android_location_get]

    private fun fillInAddress(place: Place) {
        val components = place.addressComponents

        // Get each component of the address from the place details,
        // and then fill-in the corresponding field on the form.
        // Possible AddressComponent types are documented at https://goo.gle/32SJPM1
        if (components != null) {
            with (components.toAddress()) {
                binding.autocompleteAddress1.setText(streetAddress)
                binding.autocompletePostal.setText(fullPostalCode)
                binding.autocompleteCity.setText(locality)
                binding.autocompleteState.setText(adminArea)
                binding.autocompleteCountry.setText(country)
            }
        }

        // After filling the form with address components from the Autocomplete
        // prediction, set cursor focus on the second address line to encourage
        // entry of sub-premise information such as apartment, unit, or floor number.
        binding.autocompleteAddress2.requestFocus()

        // Add a map for visual confirmation of the address
        showMap(place)
    }

    // [START maps_solutions_android_autocomplete_map_add]
    private fun showMap(place: Place) {
        coordinates = place.location as LatLng

        // It isn't possible to set a fragment's id programmatically so we set a tag instead and
        // search for it using that.
        mapFragment =
            supportFragmentManager.findFragmentByTag(MAP_FRAGMENT_TAG) as SupportMapFragment?

        // We only create a fragment if it doesn't already exist.
        if (mapFragment == null) {
            mapPanel = (findViewById<View>(R.id.stub_map) as ViewStub).inflate()
            val mapOptions = GoogleMapOptions()
            mapOptions.mapToolbarEnabled(false)

            // To programmatically add the map, we first create a SupportMapFragment.
            mapFragment = SupportMapFragment.newInstance(mapOptions)

            // Then we add it using a FragmentTransaction.
            supportFragmentManager
                .beginTransaction()
                .add(
                    R.id.confirmation_map,
                    mapFragment!!,
                    MAP_FRAGMENT_TAG
                )
                .commit()
            mapFragment!!.getMapAsync(this)
        } else {
            updateMap(coordinates)
        }
    }
    // [END maps_solutions_android_autocomplete_map_add]

    private fun updateMap(latLng: LatLng) {
        marker!!.position = latLng
        map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        if (mapPanel.isGone) {
            mapPanel.visibility = View.VISIBLE
        }
    }

    // [START maps_solutions_android_autocomplete_map_ready]
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a string resource.
            val success = map!!.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json)
            )
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
        map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 15f))
        marker = map!!.addMarker(MarkerOptions().position(coordinates))
    }
    // [END maps_solutions_android_autocomplete_map_ready]

    private fun clearForm() {
        binding.autocompleteAddress1.setText("")
        binding.autocompleteAddress2.text.clear()
        binding.autocompleteCity.text.clear()
        binding.autocompleteState.text.clear()
        binding.autocompletePostal.text.clear()
        binding.autocompleteCountry.text.clear()
        mapPanel.visibility = View.GONE
        binding.autocompleteAddress1.requestFocus()
    }

    // [START maps_solutions_android_permission_request]
    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher, as an instance variable.
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Since ACCESS_FINE_LOCATION is the only permission in this sample,
            // run the location comparison task once permission is granted.
            // Otherwise, check which permission is granted.
            getAndCompareLocations()
        } else {
            // Fallback behavior if user denies permission
            Log.d(TAG, "User denied permission")
        }
    }
    // [END maps_solutions_android_permission_request]

    companion object {
        private val TAG = AutocompleteAddressActivity::class.java.simpleName
        private const val MAP_FRAGMENT_TAG = "MAP"
    }
}

/**
 * Data class representing a postal address.
 *
 * @property streetNumber The street number of the address.
 * @property locality The locality or neighborhood of the address.
 * @property route The street name or route of the address.
 * @property postCode The primary part of the postal code.
 * @property postCodeSuffix The optional suffix or extension of the postal code.
 * @property adminArea The administrative area, such as state, province, or region.
 * @property country The country of the address.
 */
private data class Address(
    val streetNumber: String,
    val locality: String,
    val route: String,
    val postCode: String,
    val postCodeSuffix: String,
    val adminArea: String,
    val country: String
) {
    val fullPostalCode: String
        get() = listOf(postCode, postCodeSuffix).filter { it.isNotBlank() }.joinToString("-")
    val streetAddress: String
        get() = listOf(streetNumber, route).filter { it.isNotBlank() }.joinToString(" ")
}

/**
 * Converts an [AddressComponents] object to an [Address] object.
 *
 * This function iterates through the address components, creating a map where the key is the component type
 * (e.g., "street_number", "route") and the value is the component name. It then uses this map to populate
 * the fields of an [Address] object.
 *
 * If a specific address component type is not found in the [AddressComponents], the corresponding field in
 * the [Address] object will be set to an empty string.
 *
 * @return An [Address] object representing the address information extracted from the [AddressComponents].
 */
private fun AddressComponents.toAddress(): Address {
    val addressMap = this.asList().associate { it.types[0] to it.name }
    return Address(
        streetNumber = addressMap["street_number"] ?: "",
        route = addressMap["route"] ?: "",
        postCode = addressMap["postal_code"] ?: "",
        postCodeSuffix = addressMap["postal_code_suffix"] ?: "",
        locality = addressMap["locality"] ?: "",
        adminArea = addressMap["administrative_area_level_1"] ?: "",
        country = addressMap["country"] ?: ""
    )
}
// Copyright 2025 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// [START placessdkandroid_place_details_ui_kit_add_place_details_component_full]

package com.example.placedetailsuikit

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import com.example.placedetailsuikit.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.PlaceDetailsCompactFragment
import com.google.android.libraries.places.widget.PlaceLoadListener
import com.google.android.libraries.places.widget.model.Orientation

private const val TAG = "PlacesUiKit"

/**
 * A simple ViewModel to store UI state that needs to survive configuration changes.
 * In this case, it holds the ID of the selected place.
 */
class MainViewModel : ViewModel() {
    var selectedPlaceId: String? = null
}

/**
 * Main Activity for the application. This class is responsible for:
 * 1. Displaying a Google Map.
 * 2. Handling location permissions to center the map on the user's location.
 * 3. Handling clicks on Points of Interest (POIs) on the map.
 * 4. Displaying a [PlaceDetailsCompactFragment] to show details of a selected POI.
 */
class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnPoiClickListener {
    // ViewBinding for safe and easy access to views.
    private lateinit var binding: ActivityMainBinding
    private var googleMap: GoogleMap? = null

    // Client for retrieving the device's last known location.
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Modern approach for handling permission requests and their results.
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    // ViewModel to store state across configuration changes (like screen rotation).
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the permissions launcher. This defines what to do after the user
        // responds to the permission request dialog.
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                    // Permission was granted. Fetch the user's location.
                    Log.d(TAG, "Location permission granted by user.")
                    fetchLastLocation()
                } else {
                    // Permission was denied. Show a message and default to a fallback location.
                    Log.d(TAG, "Location permission denied by user.")
                    Toast.makeText(
                        this,
                        "Location permission denied. Showing default location.",
                        Toast.LENGTH_LONG
                    ).show()
                    moveToSydney()
                }
            }

        // Standard setup for ViewBinding and enabling edge-to-edge display.
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the dismiss button listener
        binding.dismissButton.setOnClickListener {
            dismissPlaceDetails()
        }

        // --- Crucial: Initialize Places SDK ---
        val apiKey = BuildConfig.PLACES_API_KEY
        if (apiKey.isEmpty() || apiKey == "YOUR_API_KEY") {
            Log.e(TAG, "No api key")
            Toast.makeText(
                this,
                "Add your own API_KEY in local.properties",
                Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        // Initialize the SDK with the application context and API key.
        Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)

        // Initialize the location client.
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // ------------------------------------

        // Obtain the SupportMapFragment and request the map asynchronously.
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        // After rotation, check if a place was selected. If so, restore the fragment.
        if (viewModel.selectedPlaceId != null) {
            viewModel.selectedPlaceId?.let { placeId ->
                Log.d(TAG, "Restoring PlaceDetailsFragment for place ID: $placeId")
                showPlaceDetailsFragment(placeId)
            }
        }
    }

    /**
     * Callback triggered when the map is ready to be used.
     */
    override fun onMapReady(map: GoogleMap) {
        Log.d(TAG, "Map is ready")
        googleMap = map
        // Set a listener for clicks on Points of Interest.
        googleMap?.setOnPoiClickListener(this)

        // Check for location permissions to determine the initial map position.
        if (isLocationPermissionGranted()) {
            fetchLastLocation()
        } else {
            requestLocationPermissions()
        }
    }

    /**
     * Checks if either fine or coarse location permission has been granted.
     */
    private fun isLocationPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Launches the permission request flow. The result is handled by the
     * ActivityResultLauncher defined in onCreate.
     */
    private fun requestLocationPermissions() {
        Log.d(TAG, "Requesting location permissions.")
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    /**
     * Fetches the device's last known location and moves the map camera to it.
     * This function should only be called after verifying permissions.
     */
    @SuppressLint("MissingPermission")
    private fun fetchLastLocation() {
        if (isLocationPermissionGranted()) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        // Move camera to user's location if available.
                        val userLocation = LatLng(location.latitude, location.longitude)
                        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 13f))
                        Log.d(TAG, "Moved to user's last known location.")
                    } else {
                        // Fallback to a default location if the last location is null.
                        Log.d(TAG, "Last known location is null. Falling back to Sydney.")
                        moveToSydney()
                    }
                }
                .addOnFailureListener {
                    // Handle errors in fetching location.
                    Log.e(TAG, "Failed to get location.", it)
                    moveToSydney()
                }
        }
    }

    /**
     * A default fallback location for the map camera.
     */
    private fun moveToSydney() {
        val sydney = LatLng(-33.8688, 151.2093)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 13f))
        Log.d(TAG, "Moved to Sydney")
    }

    /**
     * Callback for when a Point of Interest on the map is clicked.
     */
    override fun onPoiClick(poi: PointOfInterest) {
        val placeId = poi.placeId
        Log.d(TAG, "Place ID: $placeId")

        // Save the selected place ID to the ViewModel to survive rotation.
        viewModel.selectedPlaceId = placeId
        showPlaceDetailsFragment(placeId)
    }

    /**
     * Instantiates and displays the [PlaceDetailsCompactFragment].
     * @param placeId The unique identifier for the place to be displayed.
     */
    private fun showPlaceDetailsFragment(placeId: String) {
        Log.d(TAG, "Showing PlaceDetailsFragment for place ID: $placeId")

        // Show the wrapper, hide the dismiss button, and show the loading indicator.
        binding.placeDetailsWrapper.visibility = View.VISIBLE
        binding.dismissButton.visibility = View.GONE
        binding.placeDetailsContainer.visibility = View.GONE
        binding.loadingIndicatorMain.visibility = View.VISIBLE

        // Determine the orientation based on the device's current configuration.
        val orientation =
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                Orientation.HORIZONTAL
            } else {
                Orientation.VERTICAL
            }

        // [START placessdkandroid_place_details_ui_kit_add_place_details_component_snippet]
        
        // Create a new instance of the fragment from the Places SDK.
        val fragment = PlaceDetailsCompactFragment.newInstance(
            PlaceDetailsCompactFragment.ALL_CONTENT,
            orientation,
            R.style.CustomizedPlaceDetailsTheme,
        ).apply {
            // Set a listener to be notified when the place data has been loaded.
            setPlaceLoadListener(object : PlaceLoadListener {
                override fun onSuccess(place: Place) {
                    Log.d(TAG, "Place loaded: ${place.id}")
                    // Hide loader, show the fragment container and the dismiss button
                    binding.loadingIndicatorMain.visibility = View.GONE
                    binding.placeDetailsContainer.visibility = View.VISIBLE
                    binding.dismissButton.visibility = View.VISIBLE
                }

                override fun onFailure(e: Exception) {
                    Log.e(TAG, "Place failed to load", e)
                    // Hide everything on failure
                    dismissPlaceDetails()
                    Toast.makeText(this@MainActivity, "Failed to load place details.", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // Add the fragment to the container in the layout.
        supportFragmentManager
            .beginTransaction()
            .replace(binding.placeDetailsContainer.id, fragment)
            .commitNow() // Use commitNow to ensure the fragment is immediately available.

        // **This is the key step**: Tell the fragment to load data for the given Place ID.
        binding.root.post {
            fragment.loadWithPlaceId(placeId)
        }
    }

    // [END placessdkandroid_place_details_ui_kit_add_place_details_component_snippet]

    private fun dismissPlaceDetails() {
        binding.placeDetailsWrapper.visibility = View.GONE
        viewModel.selectedPlaceId = null
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear references to avoid memory leaks.
        googleMap = null
    }
}

// [END placessdkandroid_place_details_ui_kit_add_place_details_component_full]

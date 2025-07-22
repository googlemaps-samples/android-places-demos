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
package com.example.placesuikit3d

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.placesuikit3d.databinding.ActivityMainBinding
import com.example.placesuikit3d.utils.feet
import com.example.placesuikit3d.utils.toValidCamera
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.PlaceDetailsCompactFragment
import com.google.android.libraries.places.widget.PlaceLoadListener
import com.google.android.libraries.places.widget.model.Orientation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The main activity for the 3D map demo.
 *
 * This activity demonstrates how to integrate the Places UI Kit with a 3D map view.
 * It handles map initialization, location permissions, and displaying place details.
 *
 * Implements [OnMap3DViewReadyCallback] to receive the [GoogleMap3D] instance once it's ready.
 */
class MainActivity : AppCompatActivity(), OnMap3DViewReadyCallback {
    private val TAG = this::class.java.simpleName
    private lateinit var binding: ActivityMainBinding
    private var googleMap3D: GoogleMap3D? = null
    private var placeDetailsFragment: PlaceDetailsCompactFragment? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Registers a callback for the result of requesting permissions.
        // This is the modern way to handle permission requests, replacing onActivityResult.
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                    Log.d(TAG, "Location permission granted by user.")
                    fetchLastLocation()
                } else {
                    Log.d(TAG, "Location permission denied by user.")
                    Toast.makeText(
                        this,
                        "Location permission denied. Showing default location.",
                        Toast.LENGTH_SHORT
                    ).show()
                    moveToDefaultLocation()
                }
            }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Allows the app to draw behind the system bars for a more immersive experience.
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Adjusts the status bar icons for better visibility against the app's theme.
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        val isLightTheme = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_NO
        insetsController.isAppearanceLightStatusBars = isLightTheme

        // Applies window insets as padding to the content view.
        // This prevents UI elements from being obscured by the system bars.
        val contentView = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(contentView) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        // Sets up a click listener to fetch the user's location and move the camera.
        binding.myLocationButton.setOnClickListener {
            fetchLastLocation()
        }

        // Clears the selected place, which will hide the details view.
        binding.dismissButton.setOnClickListener {
            viewModel.setSelectedPlaceId(null)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Asynchronously initializes the map and registers the [OnMap3DViewReadyCallback].
        binding.map3dView.onCreate(savedInstanceState)
        binding.map3dView.getMap3DViewAsync(this)

        // Collects the selected place ID from the ViewModel.
        // This ensures the UI reacts to changes in the selected place.
        lifecycleScope.launch {
            viewModel.placeId.collect { placeId ->
                if (placeId.isNullOrEmpty()) {
                    dismissPlaceDetails()
                } else {
                    showPlaceDetailsFragment(placeId)
                }
            }
        }
    }

    /**
     * Called when the map is ready to be used.
     * This is where we configure the map's initial state and set up listeners.
     */
    override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
        this.googleMap3D = googleMap3D
        // Hybrid mode shows satellite imagery with road maps overlaid.
        googleMap3D.setMapMode(Map3DMode.HYBRID)
        // A null restriction allows the camera to move freely.
        googleMap3D.setCameraRestriction(null)
        googleMap3D.setCamera(initialCamera)

        // Sets a listener for clicks on the map.
        // This is used to select a place and show its details.
        googleMap3D.setMap3DClickListener { location: LatLngAltitude, placeId: String? ->
            Log.d(
                "MainActivity",
                "onMap3DClick: ${location.latitude}, ${location.longitude}, ${location.altitude}, $placeId",
            )

            viewModel.setSelectedPlaceId(placeId)
        }

        // After the map is ready, check for location permissions and act accordingly.
        if (isLocationPermissionGranted()) {
            fetchLastLocation()
        } else {
            requestLocationPermissions()
        }
    }

    /**
     * Checks if the app has been granted location permissions.
     * It's good practice to check for permissions before accessing location services.
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
     * Requests location permissions from the user.
     * The result is handled by the [requestPermissionLauncher].
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
     * Fetches the user's last known location and moves the camera to it.
     * This provides a quick and battery-efficient way to get the user's location.
     */
    @SuppressLint("MissingPermission")
    private fun fetchLastLocation() {
        if (isLocationPermissionGranted()) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val userLocation = latLngAltitude {
                            latitude = location.latitude
                            longitude = location.longitude
                            altitude = location.altitude
                        }
                        googleMap3D?.flyCameraTo(
                            flyToOptions {
                                endCamera = camera {
                                    center = userLocation
                                    range = 5000.0
                                    tilt = 60.0
                                }.toValidCamera()
                                durationInMillis = 3000
                            }
                        )
                        Log.d(TAG, "Moved to user's last known location.")
                    } else {
                        Log.d(TAG, "Last known location is null. Falling back to default.")
                        moveToDefaultLocation()
                    }
                }
                .addOnFailureListener {
                    Log.e(TAG, "Failed to get location.", it)
                    moveToDefaultLocation()
                }
        }
    }

    /**
     * Moves the camera to the predefined initial location.
     * This is used as a fallback when the user's location is not available.
     */
    private fun moveToDefaultLocation() {
        googleMap3D?.flyCameraTo(
            flyToOptions {
                endCamera = initialCamera
                durationInMillis = 3000
            }
        )
        googleMap3D?.setCamera(
            initialCamera
        )
        Log.d(TAG, "Moved to default location")
    }

    /**
     * Shows the [PlaceDetailsCompactFragment] for a given place ID.
     * This function handles the fragment transaction and UI visibility changes.
     */
    private fun showPlaceDetailsFragment(placeId: String) {
        Log.d(TAG, "Showing PlaceDetailsFragment for place ID: $placeId")

        lifecycleScope.launch {
            // Launch in the main dispatcher to ensure UI operations are safe.
            withContext(Dispatchers.Main) {

                // Show loading indicator and hide the main details view initially.
                binding.placeDetailsWrapper.visibility = View.VISIBLE
                binding.loadingContainer.visibility = View.VISIBLE
                binding.dismissButton.visibility = View.GONE
                binding.placeDetailsContainer.visibility = View.GONE

                // Adjust the orientation of the Place Details view based on the device's orientation.
                val orientation =
                    if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        Orientation.HORIZONTAL
                    } else {
                        Orientation.VERTICAL
                    }

                // Create a new instance of the fragment.
                placeDetailsFragment = PlaceDetailsCompactFragment.newInstance(
                    PlaceDetailsCompactFragment.ALL_CONTENT,
                    orientation,
                    R.style.CustomizedPlaceDetailsTheme,
                ).apply {
                    // Set a listener to handle the result of loading the place details.
                    setPlaceLoadListener(object : PlaceLoadListener {
                        override fun onSuccess(place: Place) {
                            Log.d(TAG, "Place loaded: ${place.id}")
                            // Once loaded, show the details and hide the loading indicator.
                            binding.loadingContainer.visibility = View.GONE
                            binding.placeDetailsContainer.visibility = View.VISIBLE
                            binding.dismissButton.visibility = View.VISIBLE
                        }

                        override fun onFailure(e: Exception) {
                            Log.e(TAG, "Place failed to load", e)
                            // If loading fails, hide the loading indicator and show a toast.
                            binding.loadingContainer.visibility = View.GONE
                            Toast.makeText(
                                this@MainActivity,
                                "Failed to load place details.",
                                Toast.LENGTH_SHORT
                            ).show()
                            // Clear the selected place to hide the details view.
                            viewModel.setSelectedPlaceId(null)
                        }
                    })
                }

                // Replace the container with the fragment.
                supportFragmentManager
                    .beginTransaction()
                    .replace(binding.placeDetailsContainer.id, placeDetailsFragment!!)
                    .commitNow()

                // Load the place details using the provided place ID.
                placeDetailsFragment?.loadWithPlaceId(placeId)
            }
        }
    }

    /**
     * Hides the place details view.
     * This is called when the selected place is cleared.
     */
    private fun dismissPlaceDetails() {
        binding.placeDetailsWrapper.visibility = View.GONE
    }

    /**
     * Forwards map errors to the superclass.
     */
    override fun onError(error: Exception) {
        Log.e(TAG, "Error loading map", error)
        super.onError(error)
    }

    /**
     * It's important to forward lifecycle events to the Map3DView.
     * This ensures that the map behaves correctly, for example, by refreshing data when the app resumes.
     */
    override fun onResume() {
        super.onResume()
        binding.map3dView.onResume()
    }

    /**
     * Forwarding onPause is crucial to stop rendering and save battery when the app is in the background.
     */
    override fun onPause() {
        super.onPause()
        binding.map3dView.onPause()
    }

    /**
     * Forwarding onDestroy cleans up map resources and prevents memory leaks.
     */
    override fun onDestroy() {
        super.onDestroy()
        binding.map3dView.onDestroy()
    }

    /**
     * Forwarding onLowMemory allows the map to release non-critical resources when the system is under memory pressure.
     */
    @Deprecated("Deprecated in Java")
    override fun onLowMemory() {
        super.onLowMemory()
        binding.map3dView.onLowMemory()
    }

    /**
     * Forwarding onSaveInstanceState ensures the map's state is saved and can be restored.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.map3dView.onSaveInstanceState(outState)
    }

    companion object {
        /**
         * Defines the initial camera position for the map.
         * This is used when the app starts or when the user's location is unavailable.
         */
        private val initialCamera: Camera = camera {
            center = latLngAltitude {
                latitude = 39.982129291022446
                longitude = -105.30156359691158
                altitude = 8148.feet.value
            }
            heading = 26.0
            tilt = 67.0
            range = 4000.0
        }.toValidCamera()
    }
}

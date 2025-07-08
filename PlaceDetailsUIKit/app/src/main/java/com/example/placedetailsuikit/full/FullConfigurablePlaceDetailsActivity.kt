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

package com.example.placedetailsuikit.full

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.placedetailsuikit.BuildConfig
import com.example.placedetailsuikit.R
import com.example.placedetailsuikit.databinding.ActivityFullConfigurableMapBinding
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
import com.google.android.libraries.places.widget.PlaceDetailsFragment
import com.google.android.libraries.places.widget.PlaceLoadListener
import com.google.android.libraries.places.widget.model.Orientation
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "FullConfigurablePlaceDetailsActivity"

/**
 * This activity demonstrates how to use the Place Details UI Kit with a Google Map.
 * It allows the user to click on a Point of Interest (POI) on the map to display its details.
 * The content displayed in the Place Details UI can be configured through a dialog.
 *
 * Key features:
 * - Displays a Google Map.
 * - Requests location permissions to center the map on the user's current location.
 * - Handles POI clicks on the map.
 * - Shows a [PlaceDetailsFragment] when a POI is clicked.
 * - Allows customization of the content displayed in the [PlaceDetailsFragment]
 * via a configuration dialog.
 * - Persists the selected place and configuration across activity recreation (e.g., orientation changes).
 */
class FullConfigurablePlaceDetailsActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnPoiClickListener {

    private lateinit var binding: ActivityFullConfigurableMapBinding
    private var googleMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    /**
     * The ViewModel that holds the state for the selected place ID and the
     * configuration of the content to be displayed.
     */
    private val viewModel: FullContentSelectionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register the permissions callback to handle the user's response to the system dialog.
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
                        Toast.LENGTH_LONG
                    ).show()
                    moveToSydney()
                }
            }

        enableEdgeToEdge()
        binding = ActivityFullConfigurableMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.dismissButton.setOnClickListener {
            dismissPlaceDetails()
        }

        // Check for a valid Places API key.
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

        // Initialize the Places SDK.
        Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Get the SupportMapFragment and request notification when the map is ready to be used.
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        // Observe changes in selected content and reload the Place Details Fragment if a place is already selected.
        lifecycleScope.launch {
            viewModel.selectedContent.collectLatest {
                viewModel.selectedPlaceId?.let { placeId ->
                    Log.d(TAG, "Content selection changed. Reloading PlaceDetailsFragment for place ID: $placeId")
                    showPlaceDetailsFragment(placeId)
                }
            }
        }

        // If a place was selected before a configuration change, restore the details view.
        if (viewModel.selectedPlaceId != null) {
            viewModel.selectedPlaceId?.let { placeId ->
                Log.d(TAG, "Restoring PlaceDetailsFragment for place ID: $placeId")
                showPlaceDetailsFragment(placeId)
            }
        }

        // Set up the button to open the content configuration dialog.
        binding.configureButton.setOnClickListener {
            showContentSelectionDialog()
        }

        // Set up the new location button to fetch and move to the last known location.
        binding.myLocationButton.setOnClickListener {
            fetchLastLocation()
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * @param map The [GoogleMap] that is ready.
     */
    override fun onMapReady(map: GoogleMap) {
        Log.d(TAG, "Map is ready")
        googleMap = map
        googleMap?.setOnPoiClickListener(this)

        // Check for location permissions and either fetch the location or request permissions.
        if (isLocationPermissionGranted()) {
            fetchLastLocation()
        } else {
            requestLocationPermissions()
        }
    }

    /**
     * Checks if the user has granted either fine or coarse location permissions.
     * @return `true` if permission is granted, `false` otherwise.
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
     * Launches the system dialog to request location permissions from the user.
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
     * Handles the error case when the current location cannot be retrieved.
     * It logs an error, displays a toast message to the user, and defaults
     * the map view to Sydney, Australia.
     */
    private fun handleLocationError() {
        Log.d(TAG, "Could not retrieve current location. Falling back to Sydney.")
        Toast.makeText(
            this,
            "Could not retrieve current location. Showing default location.",
            Toast.LENGTH_LONG
        ).show()
        moveToSydney()
    }

    /**
     * Gets the most recent location available to the device and moves the map camera to it.
     * If the location is unavailable, it falls back to a default location.
     */
    @SuppressLint("MissingPermission")
    private fun fetchLastLocation() {
        if (isLocationPermissionGranted()) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val latLng = LatLng(location.latitude, location.longitude)
                        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                        Log.d(TAG, "Moved to user's last known location.")
                    } else {
                        handleLocationError() // Use the helper function
                    }
                }
                .addOnFailureListener {
                    Log.e(TAG, "Failed to get location.", it)
                    handleLocationError() // Use the helper function
                }
        } else {
            requestLocationPermissions()
        }
    }

    /**
     * Moves the map camera to a default location (Sydney, Australia).
     */
    private fun moveToSydney() {
        val sydney = LatLng(-33.8688, 151.2093)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 13f))
        Log.d(TAG, "Moved to Sydney")
    }

    /**
     * Callback for when the user clicks on a Point of Interest (POI) on the map.
     * @param poi The [PointOfInterest] that was clicked.
     */
    override fun onPoiClick(poi: PointOfInterest) {
        val placeId = poi.placeId
        Log.d(TAG, "Place ID: $placeId")
        // Store the selected place ID in the ViewModel to survive configuration changes.
        viewModel.selectedPlaceId = placeId
        showPlaceDetailsFragment(placeId)
    }

    /**
     * Displays the [PlaceDetailsFragment] for a given place ID.
     * It handles the UI visibility for loading states and success/failure callbacks.
     *
     * @param placeId The ID of the place to display.
     */
    private fun showPlaceDetailsFragment(placeId: String) {
        Log.d(TAG, "Showing PlaceDetailsFragment for place ID: $placeId")
        binding.placeDetailsWrapper.visibility = View.VISIBLE
        binding.dismissButton.visibility = View.GONE
        binding.placeDetailsContainer.visibility = View.GONE
        binding.loadingIndicatorConfigurable.visibility = View.VISIBLE

        // Adjust the orientation of the Place Details fragment based on the device's orientation.
        val orientation =
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                Orientation.HORIZONTAL
            } else {
                Orientation.VERTICAL
            }

        // Create a new instance of the fragment with the configured content.
        val fragment = PlaceDetailsFragment.newInstance(
            viewModel.selectedContent.value.map { it.content },
            orientation,
            R.style.CustomizedPlaceDetailsTheme,
        ).apply {
            // Set a listener to handle the result of the place loading operation.
            setPlaceLoadListener(object : PlaceLoadListener {
                override fun onSuccess(place: Place) {
                    Log.d(TAG, "Place loaded: ${place.id}")
                    // Show the fragment and dismiss button, hide the loading indicator.
                    binding.loadingIndicatorConfigurable.visibility = View.GONE
                    binding.placeDetailsContainer.visibility = View.VISIBLE
                    binding.dismissButton.visibility = View.VISIBLE
                }

                override fun onFailure(e: Exception) {
                    Log.e(TAG, "Place failed to load", e)
                    // Hide the UI and show a toast message on failure.
                    dismissPlaceDetails()
                    Toast.makeText(
                        this@FullConfigurablePlaceDetailsActivity,
                        "Failed to load place details.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }

        // Add the fragment to the container.
        supportFragmentManager
            .beginTransaction()
            .replace(binding.placeDetailsContainer.id, fragment)
            .commitNow()

        // Start loading the place data after the fragment has been committed.
        binding.root.post {
            fragment.loadWithPlaceId(placeId)
        }
    }

    /**
     * Hides the Place Details UI components and clears the selected place ID from the ViewModel.
     */
    private fun dismissPlaceDetails() {
        binding.placeDetailsWrapper.visibility = View.GONE
        viewModel.selectedPlaceId = null
    }

    /**
     * Cleans up resources when the activity is destroyed.
     */
    override fun onDestroy() {
        super.onDestroy()
        googleMap = null
    }


    /**
     * Displays a dialog that allows the user to select which content types
     * (e.g., Address, Phone Number, Reviews) should be visible in the
     * [PlaceDetailsFragment]. The dialog uses Jetpack Compose for its UI.
     *
     * The selection state is managed by the [FullContentSelectionViewModel].
     * When the user clicks an item in the dialog, it toggles its selection state
     * via the ViewModel.
     */
    private fun showContentSelectionDialog() {
        val dialogView =
            LayoutInflater.from(this).inflate(R.layout.content_selector_dialog, null)
        val composeView = dialogView.findViewById<ComposeView>(R.id.compose_view)

        // Use Jetpack Compose to build the dialog's UI.
        composeView.setContent {
            val selectedContent by viewModel.selectedContent.collectAsState()
            val unselectedContent by viewModel.unselectedContent.collectAsState()
            PlaceContentSelectionDialogContent(selectedContent, unselectedContent) {
                viewModel.toggleSelection(it)
            }
        }

        // Create and show the AlertDialog.
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
            .setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
        val dialog = builder.create()
        dialog.show()
    }
}

/**
 * A Composable that displays two lists of content: selected and unselected.
 * It uses sticky headers to keep the section titles visible during scrolling.
 *
 * @param selectedContent The list of items that are currently selected.
 * @param unselectedContent The list of items that are available but not selected.
 * @param onItemClick A callback function invoked when any item is clicked.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaceContentSelectionDialogContent(
    selectedContent: List<PlaceDetailsFullItem>,
    unselectedContent: List<PlaceDetailsFullItem>,
    onItemClick: (PlaceDetailsFullItem) -> Unit
) {
    LazyColumn {
        stickyHeader {
            SectionHeader("Selected Content")
        }

        items(selectedContent, key = { it.content.name }) { content ->
            ContentItem(
                item = content,
                onItemClick = onItemClick
            )
        }

        stickyHeader {
            SectionHeader("Unselected Content")
        }

        items(unselectedContent, key = { it.content.name }) { content ->
            ContentItem(
                item = content,
                onItemClick = onItemClick
            )
        }
    }
}

/**
 * A Composable that renders a styled header for a section in the list.
 *
 * @param title The text to display in the header.
 */
@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .padding(16.dp),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onTertiaryContainer,
        textAlign = TextAlign.Center
    )
}

/**
 * A Composable that renders a single clickable item in the content selection list.
 *
 * @param item The content item to display.
 * @param onItemClick A callback function invoked when this item is clicked.
 */
@Composable
fun ContentItem(
    item: PlaceDetailsFullItem,
    onItemClick: (PlaceDetailsFullItem) -> Unit
) {
    Text(
        text = item.displayName,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(item) }
            .padding(16.dp),
        style = MaterialTheme.typography.bodyLarge
    )
}

// --- Previews ---

/**
 * A preview that displays the dialog content with items in both sections.
 */
@Preview(name = "Both Sections", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun DialogContentPreview_BothSections() {
    MaterialTheme {
        PlaceContentSelectionDialogContent(
            selectedContent = PlaceDetailsFullItem.standardContent,
            unselectedContent = PlaceDetailsFullItem.standardNonContent,
            onItemClick = {}
        )
    }
}

/**
 * A preview that displays the dialog content with only selected items.
 */
@Preview(name = "Only Selected", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun DialogContentPreview_OnlySelected() {
    MaterialTheme {
        PlaceContentSelectionDialogContent(
            selectedContent = PlaceDetailsFragment.Content.entries.toPlaceDetailsFullItems(),
            unselectedContent = emptyList(),
            onItemClick = {}
        )
    }
}

/**
 * A preview that displays the dialog content with only unselected items.
 */
@Preview(name = "Only Unselected", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun DialogContentPreview_OnlyUnselected() {
    MaterialTheme {
        PlaceContentSelectionDialogContent(
            selectedContent = emptyList(),
            unselectedContent = PlaceDetailsFragment.Content.entries.toPlaceDetailsFullItems(),
            onItemClick = {}
        )
    }
}

/**
 * A preview that displays the dialog content in its empty state.
 */
@Preview(name = "Empty State", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun DialogContentPreview_Empty() {
    MaterialTheme {
        PlaceContentSelectionDialogContent(
            selectedContent = emptyList(),
            unselectedContent = emptyList(),
            onItemClick = {}
        )
    }
}

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
 * This activity demonstrates an advanced use case of the **full-screen** Place Details UI Kit.
 * It is structurally similar to the "compact" example but uses the [PlaceDetailsFragment]
 * instead of the [com.google.android.libraries.places.widget.PlaceDetailsCompactFragment].
 *
 * Key features demonstrated:
 * - Dynamic content configuration for the full-screen widget.
 * - Use of a [FullContentSelectionViewModel] to manage UI state.
 * - Reactive UI updates using Kotlin Flows.
 */
class FullConfigurablePlaceDetailsActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnPoiClickListener {

    private lateinit var binding: ActivityFullConfigurableMapBinding
    private var googleMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    // The ViewModel holds all the state that needs to survive configuration changes.
    private val viewModel: FullContentSelectionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        // This coroutine observes the content selection from the ViewModel.
        // `collectLatest` ensures that if the user changes the selection multiple times quickly,
        // only the latest selection is used to update the UI, preventing unnecessary work.
        lifecycleScope.launch {
            viewModel.selectedContent.collectLatest {
                viewModel.selectedPlaceId?.let { placeId ->
                    Log.d(TAG, "Content selection changed. Reloading PlaceDetailsFragment for place ID: $placeId")
                    showPlaceDetailsFragment(placeId)
                }
            }
        }

        // Restore the fragment if a place was already selected before a configuration change.
        if (viewModel.selectedPlaceId != null) {
            viewModel.selectedPlaceId?.let { placeId ->
                Log.d(TAG, "Restoring PlaceDetailsFragment for place ID: $placeId")
                showPlaceDetailsFragment(placeId)
            }
        }

        binding.configureButton.setOnClickListener {
            showContentSelectionDialog()
        }

        binding.myLocationButton.setOnClickListener {
            fetchLastLocation()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        Log.d(TAG, "Map is ready")
        googleMap = map
        googleMap?.setOnPoiClickListener(this)

        if (isLocationPermissionGranted()) {
            fetchLastLocation()
        } else {
            requestLocationPermissions()
        }
    }

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

    private fun requestLocationPermissions() {
        Log.d(TAG, "Requesting location permissions.")
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun handleLocationError() {
        Log.d(TAG, "Could not retrieve current location. Falling back to Sydney.")
        Toast.makeText(
            this,
            "Could not retrieve current location. Showing default location.",
            Toast.LENGTH_LONG
        ).show()
        moveToSydney()
    }

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
                        handleLocationError()
                    }
                }
                .addOnFailureListener {
                    Log.e(TAG, "Failed to get location.", it)
                    handleLocationError()
                }
        } else {
            requestLocationPermissions()
        }
    }

    private fun moveToSydney() {
        val sydney = LatLng(-33.8688, 151.2093)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 13f))
        Log.d(TAG, "Moved to Sydney")
    }

    override fun onPoiClick(poi: PointOfInterest) {
        val placeId = poi.placeId
        Log.d(TAG, "Place ID: $placeId")
        viewModel.selectedPlaceId = placeId
        showPlaceDetailsFragment(placeId)
    }

    /**
     * Displays the [PlaceDetailsFragment] for a given place ID.
     * The content shown in the fragment is determined by the user's selection,
     * which is retrieved from the [FullContentSelectionViewModel].
     *
     * @param placeId The ID of the place to display.
     */
    private fun showPlaceDetailsFragment(placeId: String) {
        Log.d(TAG, "Showing PlaceDetailsFragment for place ID: $placeId")
        binding.placeDetailsWrapper.visibility = View.VISIBLE
        binding.dismissButton.visibility = View.GONE
        binding.placeDetailsContainer.visibility = View.GONE
        binding.loadingIndicatorConfigurable.visibility = View.VISIBLE

        val orientation =
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                Orientation.HORIZONTAL
            } else {
                Orientation.VERTICAL
            }

        // The key step: Create a new instance of the fragment, passing the list of
        // selected content from the ViewModel. This ensures the fragment respects the user's configuration.
        val fragment = PlaceDetailsFragment.newInstance(
            viewModel.selectedContent.value.map { it.content },
            orientation,
            R.style.CustomizedPlaceDetailsTheme,
        ).apply {
            setPlaceLoadListener(object : PlaceLoadListener {
                override fun onSuccess(place: Place) {
                    Log.d(TAG, "Place loaded: ${place.id}")
                    binding.loadingIndicatorConfigurable.visibility = View.GONE
                    binding.placeDetailsContainer.visibility = View.VISIBLE
                    binding.dismissButton.visibility = View.VISIBLE
                }

                override fun onFailure(e: Exception) {
                    Log.e(TAG, "Place failed to load", e)
                    dismissPlaceDetails()
                    Toast.makeText(
                        this@FullConfigurablePlaceDetailsActivity,
                        "Failed to load place details.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }

        supportFragmentManager
            .beginTransaction()
            .replace(binding.placeDetailsContainer.id, fragment)
            .commitNow()

        binding.root.post {
            fragment.loadWithPlaceId(placeId)
        }
    }

    private fun dismissPlaceDetails() {
        binding.placeDetailsWrapper.visibility = View.GONE
        viewModel.selectedPlaceId = null
    }

    override fun onDestroy() {
        super.onDestroy()
        googleMap = null
    }

    /**
     * Displays a dialog that allows the user to select which content types
     * should be visible in the [PlaceDetailsFragment].
     * This demonstrates embedding a Jetpack Compose UI inside a View-based dialog.
     */
    private fun showContentSelectionDialog() {
        val dialogView =
            LayoutInflater.from(this).inflate(R.layout.content_selector_dialog, null)
        val composeView = dialogView.findViewById<ComposeView>(R.id.compose_view)

        composeView.setContent {
            // `collectAsState` observes the ViewModel's Flows and triggers recomposition
            // automatically when the state changes.
            val selectedContent by viewModel.selectedContent.collectAsState()
            val unselectedContent by viewModel.unselectedContent.collectAsState()

            // We pass the state down to the Composable and hoist the events up to the ViewModel.
            PlaceContentSelectionDialogContent(selectedContent, unselectedContent) {
                viewModel.toggleSelection(it)
            }
        }

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}

/**
 * A Composable that displays the content selection UI.
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
// These previews allow for rapid UI development of the dialog content in various states.

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

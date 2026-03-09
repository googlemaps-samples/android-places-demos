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
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import com.example.placesuikit3d.ui.theme.PlacesUIKit3DTheme
import com.example.placesuikit3d.utils.feet
import com.example.placesuikit3d.utils.toValidCamera
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.PlaceDetailsCompactFragment
import com.google.android.libraries.places.widget.PlaceLoadListener
import com.google.android.libraries.places.widget.model.Orientation

/**
 * The main activity for the 3D map demo.
 *
 * This activity demonstrates how to integrate the Places UI Kit with a 3D map view using Jetpack Compose.
 * It handles map initialization, landmark selection, and displaying place details.
 */
class MainActivity : AppCompatActivity(), OnMap3DViewReadyCallback {
    private val TAG = this::class.java.simpleName
    private var googleMap3D: GoogleMap3D? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                    fetchLastLocation()
                } else {
                    Toast.makeText(this, "Location permission denied. Showing default location.", Toast.LENGTH_SHORT).show()
                    moveToDefaultLocation()
                }
            }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            PlacesUIKit3DTheme {
                MainScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen() {
        val landmarks = viewModel.landmarks
        val selectedPlaceId by viewModel.placeId.collectAsState()
        val scope = rememberCoroutineScope()
        val scaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = rememberStandardBottomSheetState(
                initialValue = SheetValue.PartiallyExpanded
            )
        )

        Box(modifier = Modifier.fillMaxSize()) {
            BottomSheetScaffold(
                scaffoldState = scaffoldState,
                sheetPeekHeight = 120.dp,
                sheetContent = {
                    LandmarkList(
                        landmarks = landmarks,
                        onLandmarkClick = { landmark ->
                            viewModel.selectLandmark(landmark)
                            flyToLandmark(landmark)
                            scope.launch {
                                scaffoldState.bottomSheetState.partialExpand()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.6f)
                    )
                }
            ) { _ ->
                // Map occupies full screen, ignoring scaffold padding
                Box(modifier = Modifier.fillMaxSize()) {
                    MapViewContainer()

                    FloatingActionButton(
                        onClick = { fetchLastLocation() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 48.dp, end = 16.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = "My Location")
                    }
                }
            }

            // Overlay stays on top of the scaffold (outer Box)
            if (!selectedPlaceId.isNullOrEmpty()) {
                PlaceDetailsOverlay(
                    placeId = selectedPlaceId!!,
                    onDismiss = { viewModel.setSelectedPlaceId(null) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        // Anchor above the bottom sheet peek height (120dp + 16dp margin)
                        .padding(bottom = 136.dp, start = 16.dp, end = 16.dp)
                )
            }
        }
    }

    @Composable
    fun MapViewContainer() {
        val context = LocalContext.current
        val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
        
        val map3DView = remember {
            com.google.android.gms.maps3d.Map3DView(context).apply {
                getMap3DViewAsync(this@MainActivity)
            }
        }

        androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_CREATE -> {
                        map3DView.onCreate(null)
                    }
                    Lifecycle.Event.ON_RESUME -> {
                        map3DView.onResume()
                    }
                    Lifecycle.Event.ON_PAUSE -> {
                        map3DView.onPause()
                    }
                    Lifecycle.Event.ON_DESTROY -> {
                        map3DView.onDestroy()
                    }
                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        AndroidView(
            factory = { map3DView },
            modifier = Modifier.fillMaxSize()
        )
    }

    @Composable
    fun PlaceDetailsOverlay(
        placeId: String,
        onDismiss: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        val containerId = remember { View.generateViewId() }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp)
                .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
        ) {
            AndroidView(
                factory = { ctx ->
                    FragmentContainerView(ctx).apply {
                        id = containerId
                    }
                },
                update = { view ->
                    val fragment = supportFragmentManager.findFragmentById(containerId) as? PlaceDetailsCompactFragment
                    if (fragment == null) {
                        val newFragment = PlaceDetailsCompactFragment.newInstance(
                            PlaceDetailsCompactFragment.ALL_CONTENT,
                            Orientation.VERTICAL,
                            R.style.CustomizedPlaceDetailsTheme
                        ).apply {
                            setPlaceLoadListener(object : PlaceLoadListener {
                                override fun onSuccess(place: Place) {
                                    Log.d(TAG, "Place loaded: ${place.id}")
                                }

                                override fun onFailure(e: Exception) {
                                    Log.e(TAG, "Place failed to load for ID: $placeId", e)
                                    // Don't auto-dismiss on failure to prevent "disappearing" components.
                                    // The fragment should handle its own error state.
                                }
                            })
                        }
                        supportFragmentManager.commit {
                            replace(containerId, newFragment)
                        }
                        // Tag the view with the current ID and post the load
                        view.tag = placeId
                        Log.e(TAG, "Loading new fragment for placeId: $placeId")
                        view.post { newFragment.loadWithPlaceId(placeId) }
                    } else {
                        // Crucially, ONLY load if the place actually changed
                        val currentlyLoaded = view.tag as? String
                        if (currentlyLoaded != placeId) {
                            view.tag = placeId
                            Log.e(TAG, "Updating existing fragment for placeId: $placeId")
                            fragment.loadWithPlaceId(placeId)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            FloatingActionButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_close),
                    contentDescription = "Dismiss"
                )
            }
        }

        // Clean up fragment when leaving composition
        androidx.compose.runtime.DisposableEffect(containerId) {
            onDispose {
                supportFragmentManager.findFragmentById(containerId)?.let {
                    supportFragmentManager.commit {
                        remove(it)
                    }
                }
            }
        }
    }

    private fun flyToLandmark(landmark: Landmark) {
        googleMap3D?.flyCameraTo(
            flyToOptions {
                endCamera = camera {
                    center = landmark.location
                    range = 1000.0
                    tilt = 45.0
                }.toValidCamera()
                durationInMillis = 2000
            }
        )
    }

    override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
        this.googleMap3D = googleMap3D
        googleMap3D.setMapMode(Map3DMode.HYBRID)
        googleMap3D.setCamera(initialCamera)

        googleMap3D.setMap3DClickListener { _, placeId ->
            Log.e(TAG, "Map clicked: placeId=$placeId")
            if (!placeId.isNullOrEmpty()) {
                viewModel.setSelectedPlaceId(placeId)
            }
        }

        if (isLocationPermissionGranted()) {
            fetchLastLocation()
        } else {
            requestLocationPermissions()
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissions() {
        requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    @SuppressLint("MissingPermission")
    private fun fetchLastLocation() {
        if (isLocationPermissionGranted()) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val userLocation = latLngAltitude {
                        latitude = it.latitude
                        longitude = it.longitude
                        altitude = it.altitude
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
                } ?: moveToDefaultLocation()
            }.addOnFailureListener { moveToDefaultLocation() }
        }
    }

    private fun moveToDefaultLocation() {
        googleMap3D?.flyCameraTo(flyToOptions { endCamera = initialCamera; durationInMillis = 3000 })
    }

    override fun onError(error: Exception) {
        Log.e(TAG, "Error loading map", error)
        super.onError(error)
    }

    companion object {
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

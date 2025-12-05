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

package com.example.placedetailscompose.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.placedetailscompose.viewmodels.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.ktx.utils.sphericalDistance
import com.google.maps.android.ktx.utils.withSphericalOffset
import kotlinx.coroutines.launch

/**
 * The main screen of the app. This screen shows a map and allows the user to select a
 * point of interest to see details about it.
 */
@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    val deviceLocation by viewModel.deviceLocation.collectAsState()
    val selectedPlace by viewModel.selectedPlace.collectAsState()
    val isMapFollowingUser by viewModel.isMapFollowingUser.collectAsState()
    val hasAnimatedToPlace by viewModel.hasAnimatedToPlace.collectAsState()
    
    // **View Mode State**
    // This state controls whether we show the Compact or Full version of the Place Details.
    // We use `saveable` so this preference persists across configuration changes (like rotation).
    var isFullView by rememberSaveable { mutableStateOf(false) }

    // **Coordinate Mode State**
    val isCoordinateMode by viewModel.isCoordinateMode.collectAsState()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(viewModel.sydney, 13f)
    }

    // Permission launcher (same as before)
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            viewModel.onPermissionGranted()
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.onPermissionGranted()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(selectedPlace) {
        selectedPlace?.let { place ->
            // Create a CameraPosition with the target zoom level that is shifted slightly south.
            // We use the place's LatLng.
            val latLng = place.location
            if (latLng != null) {
                val focalPoint = latLng.withSphericalOffset(300.0, 180.0)

                val placeCameraPosition = CameraPosition.builder()
                    .target(focalPoint)
                    .zoom(15f)
                    .build()

                if (hasAnimatedToPlace) {
                    cameraPositionState.move(
                        CameraUpdateFactory.newCameraPosition(placeCameraPosition)
                    )
                } else {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newCameraPosition(placeCameraPosition), 1000
                    )
                    viewModel.onAnimateToPlaceFinish()
                }
            }
        }
    }

    LaunchedEffect(deviceLocation, isMapFollowingUser) {
        deviceLocation?.let { location ->
            if (isMapFollowingUser) {
                val currentPosition = cameraPositionState.position.target
                val distance = currentPosition.sphericalDistance(location)
                if (distance > 100) { // Only animate if moved more than 100 meters
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(location, 15f)
                    )
                }
            }
        }
    }

    if (cameraPositionState.isMoving) {
        viewModel.onMapDragged()
    }
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = true,
                mapType = MapType.NORMAL
            ),
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = true,
                zoomControlsEnabled = false
            ),
            onMapLoaded = {
                // Map loaded
            },
            onPOIClick = { poi ->
                if (!isCoordinateMode) {
                    coroutineScope.launch {
                        val cameraPosition = CameraPosition.builder()
                            .target(poi.latLng)
                            .zoom(15f)
                            .build()
                        cameraPositionState.animate(
                            CameraUpdateFactory.newCameraPosition(cameraPosition), 2000
                        )
                    }
                    viewModel.onPoiClicked(poi)
                }
            },
            onMapClick = { latLng ->
                viewModel.onMapClicked(latLng)
            },
            mapColorScheme = ComposeMapColorScheme.FOLLOW_SYSTEM
        ) {
            // Map content (markers, etc.)
            selectedPlace?.location?.let {
                Circle(
                    center = it,
                    radius = 75.0,
                    fillColor = Color(0x880088FF),
                    strokeWidth = 2f,
                    strokeColor = Color(0xAA000000)
                )
            }
        }

        // **Controls Overlay**
        Column(
            modifier = Modifier
                .padding(top = 48.dp, start = 16.dp)
                .align(Alignment.TopStart)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            // **View Mode Toggle**
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = if (isFullView) "Full View" else "Compact View",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Switch(
                    checked = isFullView,
                    onCheckedChange = { isFullView = it }
                )
            }

            // **Coordinate Mode Toggle**
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isCoordinateMode) "Coords Mode" else "POI Mode",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Switch(
                    checked = isCoordinateMode,
                    onCheckedChange = { viewModel.onToggleCoordinateMode(it) }
                )
            }
        }

        // **Place Details Sheet**
        // We conditionally render the Place Details view only when a place is selected.
        selectedPlace?.let { place ->
            // **Conditional Rendering**
            // Based on the `isFullView` state, we choose which Composable to display.
            // This demonstrates how easily you can swap between different UI representations
            // of the same data in Compose.
            if (isFullView) {
                PlaceDetailsFullView(
                    place = place,
                    onDismiss = { viewModel.onDismissPlace() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .fillMaxHeight(0.9f) // Take up most of the screen
                )
            } else {
                PlaceDetailsCompactView(
                    place = place,
                    onDismiss = { viewModel.onDismissPlace() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                )
            }
        }


    }
}
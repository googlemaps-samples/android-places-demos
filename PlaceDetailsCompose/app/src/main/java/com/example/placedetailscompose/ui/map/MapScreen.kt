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
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.placedetailscompose.viewmodels.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.GoogleMap
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
    modifier: Modifier = Modifier,
    mapViewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    val deviceLocation by mapViewModel.deviceLocation.collectAsState()
    val selectedPlace by mapViewModel.selectedPlace.collectAsState()
    val isMapFollowingUser by mapViewModel.isMapFollowingUser.collectAsState()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(mapViewModel.sydney, 13f)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { /* Permissions result is handled by the flow */ }

    if (deviceLocation != null) {
        LaunchedEffect(Unit) {
            if (!hasLocationPermission(context)) {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    val hasAnimatedToPlace by mapViewModel.hasAnimatedToPlace.collectAsState()
    LaunchedEffect(selectedPlace) {
        selectedPlace?.let { place ->
            // Create a CameraPosition with the target zoom level that is shifted slightly south.
            val focalPoint = place.latLng.withSphericalOffset(300.0, 180.0)

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
                mapViewModel.onAnimateToPlaceFinish()
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
        mapViewModel.onMapDragged()
    }
    val coroutineScope = rememberCoroutineScope()
    Box(modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            onPOIClick = {
                coroutineScope.launch {
                    val cameraPosition = CameraPosition.builder()
                        .target(it.latLng)
                        .zoom(15f)
                        .build()
                    cameraPositionState.animate(
                        CameraUpdateFactory.newCameraPosition(cameraPosition), 2000
                    )
                }
                mapViewModel.onPoiClicked(it)
            },
            mapColorScheme = ComposeMapColorScheme.FOLLOW_SYSTEM
        ) {
            selectedPlace?.latLng?.let {
                Circle(
                    center = it,
                    radius = 75.0,
                    fillColor = Color(0x880088FF),
                    strokeWidth = 2f,
                    strokeColor = Color(0xAA000000)
                )
            }
        }

        FloatingActionButton(
            onClick = {
                mapViewModel.onMyLocationClicked()
                coroutineScope.launch {
                    deviceLocation?.let {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(it, 15f)
                        )
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 64.dp, end = 16.dp)
        ) {
            Icon(
                imageVector = if (isMapFollowingUser) Icons.Filled.MyLocation else Icons.Outlined.MyLocation,
                contentDescription = "My Location"
            )
        }

        val place = selectedPlace
        if (place != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                PlaceDetailsView(
                    place = place,
                    onDismiss = { mapViewModel.onDismissPlace() }
                )
                FloatingActionButton(
                    onClick = { mapViewModel.onDismissPlace() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss"
                    )
                }
            }
        }
    }
}

private fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}
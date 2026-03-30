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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.placedetailscompose.R
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
import kotlinx.coroutines.delay
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
    var isFullView by rememberSaveable { mutableStateOf(false) }

    // **Coordinate Mode State**
    val isCoordinateMode by viewModel.isCoordinateMode.collectAsState()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(viewModel.sydney, 13f)
    }

    val permissionDeniedString = stringResource(R.string.location_permission_denied)
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            viewModel.onPermissionGranted()
        } else {
            Toast.makeText(context, permissionDeniedString, Toast.LENGTH_SHORT).show()
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
            val latLng = place.location
            if (latLng != null) {
                val focalPoint = latLng.withSphericalOffset(300.0, 180.0)
                val placeCameraPosition = CameraPosition.builder()
                    .target(focalPoint)
                    .zoom(15f)
                    .build()
                if (hasAnimatedToPlace) {
                    cameraPositionState.move(CameraUpdateFactory.newCameraPosition(placeCameraPosition))
                } else {
                    cameraPositionState.animate(CameraUpdateFactory.newCameraPosition(placeCameraPosition), 1000)
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
                if (distance > 100) {
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(location, 15f))
                }
            }
        }
    }

    val selectedCompactContent by viewModel.selectedCompactContent.collectAsState()
    val selectedFullContent by viewModel.selectedFullContent.collectAsState()
    var showContentSelectionDialog by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    var showSettingsButton by remember { mutableStateOf(true) }
    LaunchedEffect(showSettingsButton) {
        if (showSettingsButton) {
            delay(5000)
            showSettingsButton = false
        }
    }

    if (cameraPositionState.isMoving) {
        // Reset the timer whenever the user is dragging the map.
        showSettingsButton = true
        viewModel.onMapDragged()
    }

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
                showSettingsButton = true
            },
            onPOIClick = { poi ->
                showSettingsButton = true
                if (!isCoordinateMode) {
                    coroutineScope.launch {
                        val cameraPosition = CameraPosition.builder()
                            .target(poi.latLng)
                            .zoom(15f)
                            .build()
                        cameraPositionState.animate(CameraUpdateFactory.newCameraPosition(cameraPosition), 2000)
                    }
                    viewModel.onPoiClicked(poi)
                }
            },
            onMapClick = { latLng ->
                showSettingsButton = true
                viewModel.onMapClicked(latLng)
            },
            mapColorScheme = ComposeMapColorScheme.FOLLOW_SYSTEM
        ) {
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

        var isControlsExpanded by rememberSaveable { mutableStateOf(false) }

        AnimatedVisibility(
            visible = showSettingsButton,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 48.dp, start = 16.dp)
                    .align(Alignment.TopStart),
                horizontalAlignment = Alignment.Start
            ) {
                FloatingActionButton(
                    onClick = {
                        isControlsExpanded = !isControlsExpanded
                        // Keep the button visible while the controls are expanded
                        showSettingsButton = true
                    },
                    modifier = Modifier.padding(bottom = 8.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Icon(
                        imageVector = if (isControlsExpanded) Icons.Default.ChevronLeft else Icons.Default.Settings,
                        contentDescription = if (isControlsExpanded) stringResource(R.string.collapse_settings) else stringResource(R.string.expand_settings)
                    )
                }

                AnimatedVisibility(
                    visible = isControlsExpanded,
                    enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
                    exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut()
                ) {
                    androidx.compose.material3.ElevatedCard(
                        modifier = Modifier
                            .padding(8.dp)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(24.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(end = 16.dp)
                                ) {
                                    Text(
                                        text = if (isFullView) stringResource(R.string.full_view) else stringResource(R.string.compact_view),
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    Switch(
                                        checked = isFullView,
                                        onCheckedChange = { isFullView = it },
                                        modifier = Modifier.scale(0.8f)
                                    )
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (isCoordinateMode) stringResource(R.string.coords_mode) else stringResource(R.string.poi_mode),
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    Switch(
                                        checked = isCoordinateMode,
                                        onCheckedChange = { viewModel.onToggleCoordinateMode(it) },
                                        modifier = Modifier.scale(0.8f)
                                    )
                                }
                            }
                            androidx.compose.material3.HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                            androidx.compose.material3.FilledTonalButton(
                                onClick = { showContentSelectionDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.select_fields))
                            }
                        }
                    }
                }
            }
        }

        selectedPlace?.let { place ->
            if (isFullView) {
                PlaceDetailsFullView(
                    place = place,
                    onDismiss = { viewModel.onDismissPlace() },
                    content = selectedFullContent,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .fillMaxHeight(0.9f)
                )
            } else {
                PlaceDetailsCompactView(
                    place = place,
                    onDismiss = { viewModel.onDismissPlace() },
                    content = selectedCompactContent,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                )
            }
        }

        if (showContentSelectionDialog) {
            if (isFullView) {
                PlaceContentSelectionDialog(
                    title = stringResource(R.string.select_full_view_fields),
                    allContent = com.google.android.libraries.places.widget.PlaceDetailsFragment.Content.values().toList(),
                    selectedContent = selectedFullContent,
                    onSelectionChanged = { viewModel.updateFullContent(it) },
                    onDismissRequest = { showContentSelectionDialog = false },
                    nameProvider = { it.name }
                )
            } else {
                PlaceContentSelectionDialog(
                    title = stringResource(R.string.select_compact_view_fields),
                    allContent = com.google.android.libraries.places.widget.PlaceDetailsCompactFragment.Content.values().toList(),
                    selectedContent = selectedCompactContent,
                    onSelectionChanged = { viewModel.updateCompactContent(it) },
                    onDismissRequest = { showContentSelectionDialog = false },
                    nameProvider = { it.name }
                )
            }
        }
    }
}
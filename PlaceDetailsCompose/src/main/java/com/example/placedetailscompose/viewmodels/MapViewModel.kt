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

package com.example.placedetailscompose.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.placedetailscompose.repository.LocationRepository
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private const val TAG = "MapViewModel"

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val locationRepository = LocationRepository(application)

    val sydney = LatLng(40.01833081193422, -105.27805050328878)

    // **Permission Handling**
    // We use a StateFlow to track whether location permissions have been granted.
    // This is crucial because we don't want to start collecting location updates
    // until we know we have the necessary permissions.
    private val _permissionGranted = MutableStateFlow(false)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val deviceLocation: StateFlow<LatLng?> = _permissionGranted
        .flatMapLatest { hasPermission ->
            // **Lazy Location Collection**
            // We use `flatMapLatest` to switch between flows based on the permission state.
            // If permission is granted, we start collecting from the repository.
            // If not, we emit `null` (or keep the previous state).
            // This prevents `SecurityException` crashes and ensures we only ask for location
            // when it's safe to do so.
            if (hasPermission) {
                locationRepository.getDeviceLocation()
            } else {
                flowOf(null)
            }
        }
        .map { it?.let { loc -> LatLng(loc.latitude, loc.longitude) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    /**
     * Called when the UI has confirmed that location permissions are granted.
     * This triggers the [deviceLocation] flow to start fetching updates.
     */
    fun onPermissionGranted() {
        _permissionGranted.value = true
    }

    private val _selectedPlace = MutableStateFlow<com.google.android.libraries.places.api.model.Place?>(null)
    val selectedPlace = _selectedPlace.asStateFlow()

    // **User Tracking State**
    // This state determines if the map camera should automatically follow the user's device location.
    // It starts as `true` (following) but can be disabled by user interaction (dragging).
    private val _isMapFollowingUser = MutableStateFlow(true)
    val isMapFollowingUser: StateFlow<Boolean> = _isMapFollowingUser.asStateFlow()

    // **Coordinate Mode State**
    // This state determines whether clicking the map triggers Place Details for the clicked coordinates.
    private val _isCoordinateMode = MutableStateFlow(false)
    val isCoordinateMode: StateFlow<Boolean> = _isCoordinateMode.asStateFlow()

    private val _hasAnimatedToPlace = MutableStateFlow(false)
    val hasAnimatedToPlace: StateFlow<Boolean> = _hasAnimatedToPlace.asStateFlow()

    fun onAnimateToPlaceFinish() {
        _hasAnimatedToPlace.value = true
    }

    /**
     * Called when the user manually drags the map.
     * We disable user tracking so the map doesn't jump back to the user's location while they are exploring.
     */
    fun onMapDragged() {
        _isMapFollowingUser.value = false
    }

    /**
     * Called when the "My Location" button is clicked.
     * We re-enable user tracking to snap the camera back to the user's location.
     */
    fun onMyLocationClicked() {
        _isMapFollowingUser.value = true
    }

    fun onPoiClicked(poi: PointOfInterest) {
        // When a POI is clicked, we create a Place object with the ID and LatLng.
        // This allows us to load details using the Place ID.
        val place = com.google.android.libraries.places.api.model.Place.builder()
            .setId(poi.placeId)
            .setLocation(poi.latLng)
            .setDisplayName(poi.name)
            .build()
        _selectedPlace.value = place
    }

    fun onMapClicked(latLng: LatLng) {
        if (_isCoordinateMode.value) {
            // In Coordinate Mode, we create a Place object with just the LatLng.
            // The Place Details UI will load details for this location.
            val place = com.google.android.libraries.places.api.model.Place.builder()
                .setLocation(latLng)
                .build()
            _selectedPlace.value = place
        }
    }

    fun onToggleCoordinateMode(enabled: Boolean) {
        _isCoordinateMode.value = enabled
        // Clear selection when switching modes to avoid confusion
        _selectedPlace.value = null
        _hasAnimatedToPlace.value = false
    }

    // **Content Selection State**
    private val _selectedCompactContent = MutableStateFlow(com.google.android.libraries.places.widget.PlaceDetailsCompactFragment.ALL_CONTENT)
    val selectedCompactContent: StateFlow<List<com.google.android.libraries.places.widget.PlaceDetailsCompactFragment.Content>> = _selectedCompactContent.asStateFlow()

    private val _selectedFullContent = MutableStateFlow(com.google.android.libraries.places.widget.PlaceDetailsFragment.STANDARD_CONTENT)
    val selectedFullContent: StateFlow<List<com.google.android.libraries.places.widget.PlaceDetailsFragment.Content>> = _selectedFullContent.asStateFlow()

    fun updateCompactContent(content: List<com.google.android.libraries.places.widget.PlaceDetailsCompactFragment.Content>) {
        _selectedCompactContent.value = content
    }

    fun updateFullContent(content: List<com.google.android.libraries.places.widget.PlaceDetailsFragment.Content>) {
        _selectedFullContent.value = content
    }

    fun onDismissPlace() {
        _selectedPlace.value = null
        _hasAnimatedToPlace.value = false
    }
}
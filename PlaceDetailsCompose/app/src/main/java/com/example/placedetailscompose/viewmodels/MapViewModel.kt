// Copyright 2025 Google LLC
// ... (omitted for brevity)
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private const val TAG = "MapViewModel"

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val locationRepository = LocationRepository(application)

    val sydney = LatLng(40.01833081193422, -105.27805050328878)

    val deviceLocation: StateFlow<LatLng?> = locationRepository.getDeviceLocation()
        .map { LatLng(it.latitude, it.longitude) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    private val _selectedPlace = MutableStateFlow<PointOfInterest?>(null)
    val selectedPlace = _selectedPlace.asStateFlow()

    private val _isMapFollowingUser = MutableStateFlow(true)
    val isMapFollowingUser: StateFlow<Boolean> = _isMapFollowingUser.asStateFlow()

    private val _hasAnimatedToPlace = MutableStateFlow(false)
    val hasAnimatedToPlace: StateFlow<Boolean> = _hasAnimatedToPlace.asStateFlow()

    fun onAnimateToPlaceFinish() {
        _hasAnimatedToPlace.value = true
    }

    fun onMapDragged() {
        _isMapFollowingUser.value = false
    }

    fun onMyLocationClicked() {
        _isMapFollowingUser.value = true
    }

    fun onPoiClicked(poi: PointOfInterest) {
        _selectedPlace.value = poi
    }

    fun onDismissPlace() {
        _selectedPlace.value = null
        _hasAnimatedToPlace.value = false
    }
}
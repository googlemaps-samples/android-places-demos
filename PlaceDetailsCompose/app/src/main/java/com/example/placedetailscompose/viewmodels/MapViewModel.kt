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

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "MapViewModel"

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    private val _sydney = LatLng(-33.8688, 151.2093)
    val sydney: LatLng
        get() = _sydney

    private val _deviceLocation = MutableStateFlow<LatLng?>(null)
    val deviceLocation = _deviceLocation

    private val _selectedPlace = MutableStateFlow<PointOfInterest?>(null)
    val selectedPlace = _selectedPlace.asStateFlow()

    @SuppressLint("MissingPermission")
    fun getDeviceLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    _deviceLocation.value = LatLng(location.latitude, location.longitude)
                } else {
                    _deviceLocation.value = _sydney
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to get location.", e)
                _deviceLocation.value = _sydney
            }
    }

    fun onPoiClicked(poi: PointOfInterest) {
        if (poi.placeId.isBlank()) {
            Log.e(TAG, "Place ID is null or blank.")
            _selectedPlace.value = null
        } else {
            _selectedPlace.value = poi
        }
    }

    fun onDismissPlace() {
        _selectedPlace.value = null
    }
}
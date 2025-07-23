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

// A ViewModel for the map screen.
class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    // A default location (Sydney, Australia) to use when the user's location is not available.
    val sydney = LatLng(-33.8688, 151.2093)

    // The user's current location.
    private val _deviceLocation = MutableStateFlow<LatLng?>(null)
    val deviceLocation = _deviceLocation.asStateFlow()

    // The currently selected place of interest.
    private val _selectedPlace = MutableStateFlow<PointOfInterest?>(null)
    val selectedPlace = _selectedPlace.asStateFlow()

    /**
     * Gets the user's last known location and updates the `deviceLocation` state flow.
     * This is called when the map is first displayed.
     */
    @SuppressLint("MissingPermission")
    fun getDeviceLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    _deviceLocation.value = LatLng(location.latitude, location.longitude)
                } else {
                    // If the location is null, fall back to the default location.
                    _deviceLocation.value = sydney
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to get location.", e)
                _deviceLocation.value = sydney
            }
    }

    /**
     * Called when a point of interest (POI) is clicked on the map.
     * This updates the `selectedPlace` state flow, which triggers the display of the
     * place details fragment.
     */
    fun onPoiClicked(poi: PointOfInterest) {
        if (poi.placeId.isBlank()) {
            Log.e(TAG, "Place ID is null or blank.")
            _selectedPlace.value = null
        } else {
            _selectedPlace.value = poi
        }
    }

    /**
     * Called when the place details fragment is dismissed.
     * This clears the `selectedPlace` state flow, which hides the fragment.
     */
    fun onDismissPlace() {
        _selectedPlace.value = null
    }
}
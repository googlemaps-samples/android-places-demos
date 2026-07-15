/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.libraries.places.samples.kotlincompose.core

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

data class LocationPreset(val name: String, val latLng: LatLng)

class LocationHelper(context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    val presets = listOf(
        LocationPreset("Sydney Opera House", LatLng(-33.8567844, 151.2152967)),
        LocationPreset("Googleplex", LatLng(37.4220, -122.0841)),
        LocationPreset("Tokyo Station", LatLng(35.681236, 139.767125)),
        LocationPreset("Empire State Building", LatLng(40.748817, -73.985428))
    )

    @SuppressLint("MissingPermission")
    fun getLastLocation(onSuccess: (LatLng) -> Unit, onFailure: () -> Unit) {
        try {
            fusedLocationClient.lastLocation.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    onSuccess(LatLng(task.result.latitude, task.result.longitude))
                } else {
                    onFailure()
                }
            }
        } catch (e: Exception) {
            onFailure()
        }
    }
}

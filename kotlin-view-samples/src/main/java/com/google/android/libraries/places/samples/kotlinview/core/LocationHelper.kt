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

package com.google.android.libraries.places.samples.kotlinview.core

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class LocationPreset(
    val name: String,
    val latLng: LatLng,
    val placeId: String? = null
)

object LocationHelper {
    val SYDNEY = LocationPreset("Sydney Opera House", LatLng(-33.8568, 151.2153), "ChIJ3S-JXYerEmsRUio6x_1nyio")
    val GOOGLEPLEX = LocationPreset("Googleplex", LatLng(37.4220, -122.0841), "ChIJj61dQgK6j4AR4GeTYWZsKWw")
    val TOKYO = LocationPreset("Tokyo Station", LatLng(35.6812, 139.7671), "ChIJbU6A1L2LGGARwRFwqfBx-2M")
    val EMPIRE_STATE = LocationPreset("Empire State Building", LatLng(40.7484, -73.9857), "ChIJmQJImgVYwokRL6xobKPAYw0")

    val PRESETS = listOf(SYDNEY, GOOGLEPLEX, TOKYO, EMPIRE_STATE)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentOrFallbackLocation(context: Context): LatLng {
        return suspendCancellableCoroutine { continuation ->
            try {
                val client: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
                client.lastLocation
                    .addOnSuccessListener { loc ->
                        if (loc != null) {
                            continuation.resume(LatLng(loc.latitude, loc.longitude))
                        } else {
                            continuation.resume(GOOGLEPLEX.latLng)
                        }
                    }
                    .addOnFailureListener {
                        continuation.resume(GOOGLEPLEX.latLng)
                    }
            } catch (e: Exception) {
                continuation.resume(GOOGLEPLEX.latLng)
            }
        }
    }
}

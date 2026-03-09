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

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps3d.model.latLngAltitude
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A simple ViewModel to hold the selected place ID.
 *
 * Using a ViewModel allows the state to survive configuration changes, like screen rotations,
 * ensuring the selected place isn't lost.
 */
class MainViewModel : ViewModel() {

    /**
     * The list of landmarks to display in the list.
     */
    val landmarks: List<Landmark> = listOf(
        Landmark(
            id = "ChIJwd_EEkfsa4cRqy6eShKXFXY",
            name = "Chautauqua Park",
            location = latLngAltitude {
                latitude = 39.9989
                longitude = -105.2828
                altitude = 1750.0
            }
        ),
        Landmark(
            id = "ChIJiTEGLibsa4cRepH7ZMFEcJ8",
            name = "Pearl Street Mall",
            location = latLngAltitude {
                latitude = 40.0177
                longitude = -105.2819
                altitude = 1620.0
            }
        ),
        Landmark(
            id = "ChIJwR6cajTsa4cR2TH0qKTVKAM",
            name = "University of Colorado Boulder",
            location = latLngAltitude {
                latitude = 40.0076
                longitude = -105.2659
                altitude = 1650.0
            }
        ),
        Landmark(
            id = "ChIJAfFnzszva4cR04sAt0lSm1g",
            name = "Boulder Reservoir",
            location = latLngAltitude {
                latitude = 40.0780
                longitude = -105.2220
                altitude = 1580.0
            }
        ),
        Landmark(
            id = "ChIJfXOTtWbsa4cRmW07qJRB6_8",
            name = "The Flatirons",
            location = latLngAltitude {
                latitude = 39.9880
                longitude = -105.2930
                altitude = 2100.0
            }
        )
    )

    /**
     * Sets the selected place ID.
     *
     * This function updates the `_placeId` StateFlow with the provided `placeId`.
     * If `placeId` is null, it means no place is currently selected.
     *
     * @param placeId The ID of the selected place, or null if no place is selected.
     */
    fun setSelectedPlaceId(placeId: String?) {
        _placeId.value = placeId
    }

    /**
     * The ID of the place to display.
     * This is a private mutable state flow that can be updated by the ViewModel.
     */
    private val _placeId = MutableStateFlow<String?>(null)

    /**
     * The unique identifier of the place to display in the Place Details view.
     * This is a StateFlow that can be observed for changes.
     */
    val placeId: StateFlow<String?> = _placeId.asStateFlow()

    private val _selectedLandmark = MutableStateFlow<Landmark?>(null)
    val selectedLandmark: StateFlow<Landmark?> = _selectedLandmark.asStateFlow()

    fun selectLandmark(landmark: Landmark) {
        _selectedLandmark.value = landmark
        setSelectedPlaceId(landmark.id)
    }
}
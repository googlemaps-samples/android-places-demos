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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A simple ViewModel to hold the selected place ID.
 *
 * Using a ViewModel allows the state to survive configuration changes, like screen rotations,
 * ensuring the selected place isn't lost.
 */
class MainViewModel : ViewModel() {

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
    val placeId = _placeId.asStateFlow()
}
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

package com.example.placedetailsuikit

import androidx.lifecycle.ViewModel
import com.google.android.libraries.places.widget.PlaceDetailsCompactFragment.Content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PlaceDetailsCompactItem(
    val content: Content,
    val displayName: String
)

private fun Content.toPlaceDetailsCompactItem(): PlaceDetailsCompactItem =
    PlaceDetailsCompactItem(this, this.getDisplayName())

/**
 * Formats the enum entry name into a user-friendly, readable string.
 * Example: ACCESSIBLE_ENTRANCE_ICON -> "Accessible entrance icon"
 */
fun Content.getDisplayName(): String {
    return this.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
}

/**
 * Holds the state for the Place Details content selection UI.
 */
class ContentSelectionViewModel : ViewModel() {
    var selectedPlaceId: String? = null

    val standardContent = listOf(
        Content.MEDIA,
        Content.RATING,
        Content.TYPE,
        Content.PRICE,
        Content.ACCESSIBLE_ENTRANCE_ICON,
        Content.OPEN_NOW_STATUS,
    )

    val standardNonContent = Content.entries.filter { !standardContent.contains(it) }

    // Private mutable state flow for selected items
    private val _selectedContent =
        MutableStateFlow<List<PlaceDetailsCompactItem>>(standardContent.map { it.toPlaceDetailsCompactItem() })

    // Publicly exposed read-only state flow for selected items
    val selectedContent = _selectedContent.asStateFlow()

    // Private mutable state flow for unselected items
    private val _unselectedContent =
        MutableStateFlow(standardNonContent.map { it.toPlaceDetailsCompactItem() })

    // Publicly exposed read-only state flow for unselected items
    val unselectedContent = _unselectedContent.asStateFlow()

    /**
     * Moves a content option from one list to the other (selected to unselected, or vice-versa).
     * @param content The Content item to move.
     */
    fun toggleSelection(content: PlaceDetailsCompactItem) {
        _selectedContent.update { currentSelected ->
            if (currentSelected.contains(content)) {
                // If it's already selected, remove it
                currentSelected - content
            } else {
                // If it's not selected, add it
                currentSelected + content
            }
        }

        _unselectedContent.update { currentUnselected ->
            if (currentUnselected.contains(content)) {
                // If it's in unselected, remove it
                currentUnselected - content
            } else {
                // If it's not in unselected, add it back
                currentUnselected + content
            }
        }
    }
}

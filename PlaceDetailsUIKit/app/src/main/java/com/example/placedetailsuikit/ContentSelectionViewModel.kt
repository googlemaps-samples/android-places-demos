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

/**
 * This file contains the ViewModel and related data classes/extensions for managing
 * the state of the Place Details content configuration UI.
 */
package com.example.placedetailsuikit

import androidx.lifecycle.ViewModel
import com.google.android.libraries.places.widget.PlaceDetailsCompactFragment.Content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * A data class that wraps a [Content] enum with a user-friendly [displayName].
 * This is used to populate the content selection list in the UI.
 *
 * @param content The actual [Content] enum value from the Place Details library.
 * @param displayName A formatted, human-readable string for the content type.
 */
data class PlaceDetailsCompactItem(
    val content: Content,
    val displayName: String
)

/**
 * A convenience extension function to convert a [Content] enum into a
 * [PlaceDetailsCompactItem].
 */
private fun Content.toPlaceDetailsCompactItem() =
    PlaceDetailsCompactItem(this, this.getDisplayName())

/**
 * Formats the enum entry name into a user-friendly, readable string.
 * Example: `ACCESSIBLE_ENTRANCE_ICON` becomes "Accessible entrance icon".
 *
 * @return A capitalized, space-separated string representation of the enum name.
 */
fun Content.getDisplayName() = this.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }

/**
 * A convenience extension function to convert an iterable collection of [Content] enums
 * into a list of [PlaceDetailsCompactItem]s.
 */
fun Iterable<Content>.toPlaceDetailsCompactItems() = this.map { it.toPlaceDetailsCompactItem() }

/**
 * A default list of [Content] types that are commonly displayed in the Place Details view.
 */
val standardContent = listOf(
    Content.MEDIA,
    Content.RATING,
    Content.TYPE,
    Content.PRICE,
    Content.ACCESSIBLE_ENTRANCE_ICON,
    Content.OPEN_NOW_STATUS,
)

/**
 * A list containing all [Content] types that are not in the [standardContent] list.
 */
val standardNonContent = Content.entries.filter { !standardContent.contains(it) }

/**
 * A [ViewModel] responsible for holding and managing the UI-related data for the
 * Place Details content selection. It survives configuration changes, ensuring that
 * the user's selections and the currently displayed place are not lost.
 */
class ContentSelectionViewModel : ViewModel() {
    /**
     * The ID of the place currently being displayed. This is preserved across
     * configuration changes to allow the UI to be restored automatically.
     */
    var selectedPlaceId: String? = null

    /**
     * Private mutable state flow that holds the list of currently *selected* content items.
     * This is the single source of truth for the selected items.
     */
    private val _selectedContent =
        MutableStateFlow(standardContent.map { it.toPlaceDetailsCompactItem() })

    /**
     * Publicly exposed, read-only [StateFlow] for the list of selected content items.
     * UI components should observe this flow to react to changes.
     */
    val selectedContent = _selectedContent.asStateFlow()

    /**
     * Private mutable state flow that holds the list of currently *unselected* content items.
     */
    private val _unselectedContent =
        MutableStateFlow(standardNonContent.map { it.toPlaceDetailsCompactItem() })

    /**
     * Publicly exposed, read-only [StateFlow] for the list of unselected content items.
     */
    val unselectedContent = _unselectedContent.asStateFlow()

    /**
     * Toggles the selection status of a given content item.
     * If the item is in the selected list, it's moved to the unselected list, and vice versa.
     *
     * @param content The [PlaceDetailsCompactItem] to move between lists.
     */
    fun toggleSelection(content: PlaceDetailsCompactItem) {
        // Atomically update the selected content list.
        _selectedContent.update { currentSelected ->
            if (currentSelected.contains(content)) {
                // If it's already selected, create a new list without it.
                currentSelected - content
            } else {
                // If it's not selected, create a new list with it added.
                currentSelected + content
            }
        }

        // Atomically update the unselected content list.
        _unselectedContent.update { currentUnselected ->
            if (currentUnselected.contains(content)) {
                // If it's in the unselected list, remove it.
                currentUnselected - content
            } else {
                // If it was moved from the selected list, add it back to unselected.
                currentUnselected + content
            }
        }
    }
}

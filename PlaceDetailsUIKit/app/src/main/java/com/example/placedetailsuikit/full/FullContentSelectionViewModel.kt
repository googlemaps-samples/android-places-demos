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
 * the state of the Place Details content configuration UI for the full PlaceDetailsFragment.
 */
package com.example.placedetailsuikit.full

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.places.widget.PlaceDetailsFragment.Companion.STANDARD_CONTENT
import com.google.android.libraries.places.widget.PlaceDetailsFragment.Content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * A data class that wraps a [Content] enum with a user-friendly [displayName].
 * This is used to populate the content selection list in the UI.
 *
 * @param content The actual [Content] enum value from the Place Details library.
 * @param displayName A formatted, human-readable string for the content type.
 */
data class PlaceDetailsFullItem(
    val content: Content,
    val displayName: String,
    val isSelected: Boolean = false
) {
    companion object {
        /** Default selected content for PlaceDetailsFragment. */
        val standardContent = STANDARD_CONTENT.toPlaceDetailsFullItems()

        /** Content not in the default selection. */
        val standardNonContent = Content.entries.filter { !standardContent.map { it.content }.contains(it) }
            .toPlaceDetailsFullItems()
    }
}

/**
 * A convenience extension function to convert a [Content] enum into a
 * [PlaceDetailsFullItem].
 */
private fun Content.toPlaceDetailsFullItem() =
    PlaceDetailsFullItem(this, this.getDisplayName())

/**
 * Formats the enum entry name into a user-friendly, readable string.
 * Example: `ACCESSIBLE_ENTRANCE_ICON` becomes "Accessible entrance icon".
 *
 * @return A capitalized, space-separated string representation of the enum name.
 */
fun Content.getDisplayName() = this.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }

/**
 * A convenience extension function to convert an iterable collection of [Content] enums
 * into a list of [PlaceDetailsFullItem]s.
 */
fun Iterable<Content>.toPlaceDetailsFullItems() = this.map { it.toPlaceDetailsFullItem() }

/**
 * A [ViewModel] responsible for holding and managing the UI-related data for the
 * Place Details content selection. It survives configuration changes, ensuring that
 * the user's selections and the currently displayed place are not lost.
 */
class FullContentSelectionViewModel : ViewModel() {
    /**
     * The ID of the place currently being displayed. This is preserved across
     * configuration changes to allow the UI to be restored automatically.
     */
    var selectedPlaceId: String? = null

    /**
     * A [MutableStateFlow] that holds the complete list of [PlaceDetailsFullItem]s,
     * representing all available content types. This is the single source of truth for
     * managing the selection state of each content item.
     *
     * It is initialized by mapping all entries of the [Content] enum to
     * [PlaceDetailsFullItem] objects. The initial selection state ([PlaceDetailsFullItem.isSelected])
     * is determined by whether the [Content] is present in the [PlaceDetailsFullItem.standardContent] list.
     */
    private val _contentItems = MutableStateFlow(
        Content.entries.map {
            PlaceDetailsFullItem(
                content = it,
                displayName = it.getDisplayName(),
                isSelected = PlaceDetailsFullItem.standardContent.map { it.content }.contains(it)
            )
        }
    )

    /**
     * A [StateFlow] that emits the current list of [PlaceDetailsFullItem]s
     * that are marked as selected. This flow is derived from [_contentItems]
     * and updates automatically whenever an item's selection state changes.
     * It is eagerly started and will retain the last emitted list.
     */
    val selectedContent: StateFlow<List<PlaceDetailsFullItem>> =
        _contentItems.map { items -> items.filter { it.isSelected } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * A [StateFlow] that provides a read-only list of [PlaceDetailsFullItem]s
     * that are currently *not* selected by the user. This flow is derived from
     * the [_contentItems] flow and updates automatically whenever the selection
     * state of any item changes. It is eagerly started and defaults to an empty list.
     */
    val unselectedContent: StateFlow<List<PlaceDetailsFullItem>> =
        _contentItems.map { items -> items.filter { !it.isSelected } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * Toggles the selection status of a given content item.
     * If the item is in the selected list, it's moved to the unselected list, and vice versa.
     *
     * @param itemToToggle The [PlaceDetailsFullItem] to move between lists.
     */
    fun toggleSelection(itemToToggle: PlaceDetailsFullItem) {
        _contentItems.update { currentItems ->
            currentItems.map {
                if (it.content == itemToToggle.content) {
                    it.copy(isSelected = !it.isSelected)
                } else {
                    it
                }
            }
        }
    }
}

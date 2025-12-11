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

package com.example.placedetailsuikit.full

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.places.widget.PlaceDetailsFragment
import com.google.android.libraries.places.widget.PlaceDetailsFragment.Content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * A data class that represents a single configurable item for the [PlaceDetailsFragment].
 * It wraps the library's [Content] enum with additional properties needed for the UI.
 *
 * @param content The actual [Content] enum value from the Place Details library.
 * @param displayName A formatted, human-readable string for the content type.
 * @param isSelected A boolean indicating whether the user has selected this content to be displayed.
 */
data class PlaceDetailsFullItem(
    val content: Content,
    val displayName: String,
    val isSelected: Boolean = false
) {
    companion object {
        /**
         * The default list of content fields displayed by the [PlaceDetailsFragment].
         * We use this to set the initial state of the configuration dialog.
         */
        val standardContent: List<PlaceDetailsFullItem> = PlaceDetailsFragment.STANDARD_CONTENT.toPlaceDetailsFullItems()

        /**
         * A list of all available content fields that are not included in the default set.
         */
        val standardNonContent: List<PlaceDetailsFullItem> =
            Content.entries.filterNot { content ->
                standardContent.any { it.content == content }
            }.toPlaceDetailsFullItems()
    }
}

/**
 * An extension function to convert a [Content] enum into a [PlaceDetailsFullItem].
 */
private fun Content.toPlaceDetailsFullItem(): PlaceDetailsFullItem =
    PlaceDetailsFullItem(
        content = this,
        displayName = this.getDisplayName(),
        isSelected = PlaceDetailsFragment.STANDARD_CONTENT.contains(this)
    )

/**
 * An extension function that formats a [Content] enum name into a user-friendly, readable string.
 * For example, `REVIEWS` becomes "Reviews".
 *
 * @return A capitalized, space-separated string representation of the enum name.
 */
fun Content.getDisplayName(): String =
    this.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }

/**
 * An extension function to convert a collection of [Content] enums into a list of
 * [PlaceDetailsFullItem]s, ready to be used by the UI.
 */
fun Iterable<Content>.toPlaceDetailsFullItems(): List<PlaceDetailsFullItem> =
    this.map { it.toPlaceDetailsFullItem() }

/**
 * A [ViewModel] for the [FullConfigurablePlaceDetailsActivity] that manages the
 * state for the content selection UI. It follows the same reactive pattern as the
 * compact version's ViewModel.
 */
class FullContentSelectionViewModel : ViewModel() {
    /**
     * The ID of the place currently being displayed. Stored in the ViewModel to
     * survive configuration changes.
     */
    var selectedPlaceId: String? = null

    /**
     * The private, mutable state holder for the list of all content items. This is the
     * single source of truth for the selection state.
     */
    private val _contentItems = MutableStateFlow(
        Content.entries.map {
            PlaceDetailsFullItem(
                content = it,
                displayName = it.getDisplayName(),
                isSelected = PlaceDetailsFragment.STANDARD_CONTENT.contains(it)
            )
        }
    )

    /**
     * A read-only `StateFlow` that exposes the list of currently **selected** content items.
     * The UI collects this flow to display the list of selected items.
     */
    val selectedContent: StateFlow<List<PlaceDetailsFullItem>> =
        _contentItems.map { items -> items.filter { it.isSelected } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * A read-only `StateFlow` that exposes the list of currently **unselected** content items.
     * The UI collects this flow to display the list of available items.
     */
    val unselectedContent: StateFlow<List<PlaceDetailsFullItem>> =
        _contentItems.map { items -> items.filterNot { it.isSelected } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * This function handles the logic of toggling an item's selection status. It is called
     * from the UI when a user interacts with the selection dialog.
     *
     * @param itemToToggle The [PlaceDetailsFullItem] that the user clicked.
     */
    fun toggleSelection(itemToToggle: PlaceDetailsFullItem) {
        _contentItems.update { currentItems ->
            currentItems.map { item ->
                if (item.content == itemToToggle.content) {
                    item.copy(isSelected = !item.isSelected)
                } else {
                    item
                }
            }
        }
    }
}

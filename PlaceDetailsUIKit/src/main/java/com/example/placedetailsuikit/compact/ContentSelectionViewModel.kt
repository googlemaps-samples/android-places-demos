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

package com.example.placedetailsuikit.compact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.places.widget.PlaceDetailsCompactFragment
import com.google.android.libraries.places.widget.PlaceDetailsCompactFragment.Content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * A data class that represents a single configurable item for the [PlaceDetailsCompactFragment].
 * It wraps the library's [Content] enum with additional properties needed for the UI,
 * such as a user-friendly display name and its current selection state.
 *
 * @param content The actual [Content] enum value from the Place Details library.
 * @param displayName A formatted, human-readable string for the content type.
 * @param isSelected A boolean indicating whether the user has selected this content to be displayed.
 */
data class PlaceDetailsCompactItem(
    val content: Content,
    val displayName: String,
    val isSelected: Boolean = false
)

/**
 * An extension function to convert a [Content] enum into a [PlaceDetailsCompactItem].
 * This simplifies the creation of UI models from the library's data model.
 */
private fun Content.toPlaceDetailsCompactItem() =
    PlaceDetailsCompactItem(
        content = this,
        displayName = this.getDisplayName(),
        isSelected = standardContent.contains(this)
    )

/**
 * An extension function that formats a [Content] enum name into a user-friendly, readable string.
 * For example, `ACCESSIBLE_ENTRANCE_ICON` becomes "Accessible entrance icon".
 *
 * @return A capitalized, space-separated string representation of the enum name.
 */
fun Content.getDisplayName(): String =
    this.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }

/**
 * An extension function to convert a collection of [Content] enums into a list of
 * [PlaceDetailsCompactItem]s, ready to be used by the UI.
 */
fun Iterable<Content>.toPlaceDetailsCompactItems(): List<PlaceDetailsCompactItem> =
    this.map { it.toPlaceDetailsCompactItem() }

/**
 * A default list of [Content] types that are commonly displayed in the Place Details view.
 * This is defined by the Places SDK.
 */
val standardContent: List<Content> = PlaceDetailsCompactFragment.STANDARD_CONTENT

/**
 * A list containing all [Content] types that are *not* in the [standardContent] list.
 * This is used to populate the "Unselected" section of the configuration dialog initially.
 */
val standardNonContent: List<Content> = Content.entries.filter { !standardContent.contains(it) }

/**
 * A [ViewModel] responsible for holding and managing the UI state for the
 * Place Details content selection feature. It uses Kotlin Flows to create a reactive
 * data layer that the UI can observe.
 *
 * Key responsibilities:
 * - Storing the `selectedPlaceId` across configuration changes.
 * - Maintaining the single source of truth for all available content items and their selection states.
 * - Exposing `StateFlow`s that the UI can collect to automatically update when the state changes.
 * - Providing a method (`toggleSelection`) to handle user interactions from the UI.
 */
class ContentSelectionViewModel : ViewModel() {
    /**
     * The ID of the place currently being displayed. This is a simple `var` because its
     * state is managed directly by the Activity, but it's placed in the ViewModel
     * to survive configuration changes.
     */
    var selectedPlaceId: String? = null

    /**
     * The private, mutable state holder for the list of all content items.
     * This is the **single source of truth**. All other flows are derived from this one.
     * It's initialized with all possible `Content` types, with their `isSelected`
     * property determined by whether they are in the `standardContent` list.
     */
    private val _contentItems = MutableStateFlow(
        Content.entries.map {
            PlaceDetailsCompactItem(
                content = it,
                displayName = it.getDisplayName(),
                isSelected = standardContent.contains(it)
            )
        }
    )

    /**
     * A read-only `StateFlow` that exposes the list of currently **selected** content items.
     * - It's derived from `_contentItems` using the `map` operator.
     * - `stateIn` converts this cold Flow into a hot `StateFlow`, meaning it will always have a value.
     * - `viewModelScope` ensures the flow is active as long as the ViewModel is alive.
     * - `SharingStarted.Eagerly` means the flow starts immediately and keeps its last value even if there are no collectors.
     * The UI will collect this flow to display the list of selected items.
     */
    val selectedContent: StateFlow<List<PlaceDetailsCompactItem>> =
        _contentItems.map { items -> items.filter { it.isSelected } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * A read-only `StateFlow` that exposes the list of currently **unselected** content items.
     * This is also derived from the single source of truth, `_contentItems`.
     * The UI will collect this flow to display the list of available items that the user can add.
     */
    val unselectedContent: StateFlow<List<PlaceDetailsCompactItem>> =
        _contentItems.map { items -> items.filter { !it.isSelected } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * This is the public function that the UI calls to modify the state.
     * It handles the business logic of toggling an item's selection status.
     *
     * @param itemToToggle The [PlaceDetailsCompactItem] that the user clicked.
     */
    fun toggleSelection(itemToToggle: PlaceDetailsCompactItem) {
        // `update` is a thread-safe way to modify the value of a MutableStateFlow.
        _contentItems.update { currentItems ->
            // We create a new list by mapping over the old one.
            // This ensures that we are working with immutable state, which is a core
            // principle of modern Android development and reactive programming.
            currentItems.map { item ->
                if (item.content == itemToToggle.content) {
                    // If we find the item that was clicked, we create a new `copy` of it
                    // with the `isSelected` property flipped.
                    item.copy(isSelected = !item.isSelected)
                } else {
                    // Otherwise, we return the item unmodified.
                    item
                }
            }
        }
    }
}

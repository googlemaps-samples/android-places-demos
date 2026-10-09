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

package com.google.places.android.ktx.demo

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.google.android.libraries.places.api.net.kotlin.awaitIsOpen
import com.google.android.libraries.places.api.net.kotlin.awaitSearchByText
import com.google.android.libraries.places.api.net.kotlin.awaitSearchNearby
import com.google.android.libraries.places.api.net.kotlin.searchByTextRequest
import com.google.android.libraries.places.api.net.kotlin.searchNearbyRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Immutable UI state for the Text Search, Nearby Search, and Rich Place Attributes KTX demo.
 *
 * @property isLoading Whether a coroutine search or `isOpen` request is in flight.
 * @property places The list of [Place] records returned by the most recent search.
 * @property selectedPlace The currently inspected [Place] showing 5.3.0 attributes (`AddressDescriptor`,
 *   `EVChargeOptions`, `AccessibilityOptions`, `ContainingPlace`).
 * @property isOpenStatus Tri-state result (`true`, `false`, or `null` if unknown/not checked) from
 *   [awaitIsOpen] for [selectedPlace].
 * @property errorMessage Human-readable error message when input validation or an API request fails.
 */
data class TextAndNearbySearchUiState(
    val isLoading: Boolean = false,
    val places: List<Place> = emptyList(),
    val selectedPlace: Place? = null,
    val isOpenStatus: Boolean? = null,
    val errorMessage: String? = null
)

/**
 * ViewModel demonstrating Places SDK KTX suspending extensions for Text Search ([awaitSearchByText]),
 * Nearby Search ([awaitSearchNearby]), and Open Status ([awaitIsOpen]), combined with Places SDK
 * 5.3.0 rich place fields (`ADDRESS_DESCRIPTOR`, `CONTAINING_PLACES`, `EV_CHARGE_OPTIONS`, and
 * `ACCESSIBILITY_OPTIONS`).
 *
 * ## Architectural Design
 * - **Unidirectional Data Flow (UDF)**: All state mutations flow through [_uiState] and are exposed
 *   as a read-only [StateFlow] collected with lifecycle awareness by the Compose UI.
 * - **Structured Concurrency**: Suspending SDK calls execute within [viewModelScope] and propagate
 *   [CancellationException] properly.
 */
@HiltViewModel
class PlacesTextAndNearbySearchViewModel @Inject constructor(
    private val placesClient: PlacesClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(TextAndNearbySearchUiState())
    val uiState: StateFlow<TextAndNearbySearchUiState> = _uiState.asStateFlow()

    /**
     * Executes a coroutine-based Text Search via [awaitSearchByText].
     */
    fun searchByText(
        query: String,
        center: LatLng = DEFAULT_SYDNEY_CENTER,
        radiusMeters: Double = DEFAULT_RADIUS_METERS,
        includedType: String? = "restaurant",
        openNow: Boolean = false
    ) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            _uiState.update {
                it.copy(errorMessage = "Please enter a non-empty text search query.")
            }
            return
        }
        if (radiusMeters <= 0.0) {
            _uiState.update {
                it.copy(errorMessage = "Radius must be a positive number in meters.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    selectedPlace = null,
                    isOpenStatus = null
                )
            }
            try {
                val bounds = CircularBounds.newInstance(center, radiusMeters)
                val response = placesClient.awaitSearchByText(trimmedQuery, SEARCH_PLACE_FIELDS) {
                    locationBias = bounds
                    isOpenNow = openNow
                    maxResultCount = DEFAULT_MAX_RESULTS
                    rankPreference = SearchByTextRequest.RankPreference.RELEVANCE
                    val cleanType = includedType?.trim()
                    if (!cleanType.isNullOrEmpty()) {
                        this.includedType = cleanType
                    }
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        places = response.places,
                        selectedPlace = response.places.firstOrNull()
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Text Search failed: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Executes a coroutine-based Nearby Search via [awaitSearchNearby].
     */
    fun searchNearby(
        center: LatLng = DEFAULT_SYDNEY_CENTER,
        radiusMeters: Double = DEFAULT_RADIUS_METERS,
        includedType: String? = "restaurant"
    ) {
        if (radiusMeters <= 0.0) {
            _uiState.update {
                it.copy(errorMessage = "Radius must be a positive number in meters.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    selectedPlace = null,
                    isOpenStatus = null
                )
            }
            try {
                val bounds = CircularBounds.newInstance(center, radiusMeters)
                val response = placesClient.awaitSearchNearby(bounds, SEARCH_PLACE_FIELDS) {
                    maxResultCount = DEFAULT_MAX_RESULTS
                    rankPreference = SearchNearbyRequest.RankPreference.POPULARITY
                    val cleanType = includedType?.trim()
                    if (!cleanType.isNullOrEmpty()) {
                        includedTypes = listOf(cleanType)
                    }
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        places = response.places,
                        selectedPlace = response.places.firstOrNull()
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Nearby Search failed: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Builds a [SearchByTextRequest] using the KTX [searchByTextRequest] DSL builder.
     */
    @VisibleForTesting
    internal fun buildSearchByTextRequest(
        query: String,
        center: LatLng = DEFAULT_SYDNEY_CENTER,
        radiusMeters: Double = DEFAULT_RADIUS_METERS,
        includedType: String? = "restaurant",
        openNow: Boolean = false
    ): SearchByTextRequest {
        val bounds = CircularBounds.newInstance(center, radiusMeters)
        return searchByTextRequest(query.trim(), SEARCH_PLACE_FIELDS) {
            locationBias = bounds
            isOpenNow = openNow
            maxResultCount = DEFAULT_MAX_RESULTS
            rankPreference = SearchByTextRequest.RankPreference.RELEVANCE
            val cleanType = includedType?.trim()
            if (!cleanType.isNullOrEmpty()) {
                this.includedType = cleanType
            }
        }
    }

    /**
     * Builds a [SearchNearbyRequest] using the KTX [searchNearbyRequest] DSL builder.
     */
    @VisibleForTesting
    internal fun buildSearchNearbyRequest(
        center: LatLng = DEFAULT_SYDNEY_CENTER,
        radiusMeters: Double = DEFAULT_RADIUS_METERS,
        includedType: String? = "restaurant"
    ): SearchNearbyRequest {
        val bounds = CircularBounds.newInstance(center, radiusMeters)
        return searchNearbyRequest(bounds, SEARCH_PLACE_FIELDS) {
            maxResultCount = DEFAULT_MAX_RESULTS
            rankPreference = SearchNearbyRequest.RankPreference.POPULARITY
            val cleanType = includedType?.trim()
            if (!cleanType.isNullOrEmpty()) {
                includedTypes = listOf(cleanType)
            }
        }
    }

    /**
     * Selects a [Place] from the current results list and checks its live open status using
     * [awaitIsOpen].
     */
    fun selectPlaceAndCheckOpenStatus(place: Place) {
        _uiState.update {
            it.copy(selectedPlace = place, isOpenStatus = null, errorMessage = null)
        }
        val placeId = place.id ?: return
        viewModelScope.launch {
            try {
                val response = placesClient.awaitIsOpen(placeId)
                _uiState.update {
                    it.copy(isOpenStatus = response.isOpen)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(errorMessage = "Failed to check isOpen status: ${e.message}")
                }
            }
        }
    }

    companion object {
        val DEFAULT_SYDNEY_CENTER = LatLng(-33.8567844, 151.2152967)
        const val DEFAULT_RADIUS_METERS = 2000.0
        const val DEFAULT_MAX_RESULTS = 10

        val SEARCH_PLACE_FIELDS = listOf(
            Place.Field.ID,
            Place.Field.DISPLAY_NAME,
            Place.Field.FORMATTED_ADDRESS,
            Place.Field.LOCATION,
            Place.Field.RATING,
            Place.Field.PRICE_LEVEL,
            Place.Field.TYPES,
            Place.Field.ADDRESS_DESCRIPTOR,
            Place.Field.CONTAINING_PLACES,
            Place.Field.EV_CHARGE_OPTIONS,
            Place.Field.ACCESSIBILITY_OPTIONS
        )
    }
}

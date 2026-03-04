// Copyright 2023 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.places.android.ktx.demo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.LocationBias
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.kotlin.awaitFetchPlace
import com.google.android.libraries.places.api.net.kotlin.awaitFindAutocompletePredictions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class PlacesSearchViewModel @Inject constructor(
    private val placesClient: PlacesClient
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private var sessionToken: AutocompleteSessionToken? = null

    /**
     * Exposes a StateFlow of [PlacesSearchEvent] based on the current search query.
     * Uses [debounce(300)] to strike a balance between real-time feedback and
     * minimizing redundant network calls (and costs) while the user is typing.
     */
    val searchEvents: StateFlow<PlacesSearchEvent> = _searchQuery
        .debounce(300)
        .distinctUntilChanged()
        .mapLatest { query ->
            if (query.isBlank()) {
                return@mapLatest PlacesSearchEventIdle
            }

            try {
                val bias: LocationBias = RectangularBounds.newInstance(
                    LatLng(37.7576948, -122.4727051), // SW lat, lng
                    LatLng(37.808300, -122.391338) // NE lat, lng
                )

                // Using a session token to group autocomplete queries and place fetches for billing
                if (sessionToken == null) {
                    sessionToken = AutocompleteSessionToken.newInstance()
                }

                // Using the official SDK-provided awaitFindAutocompletePredictions extension
                val response = placesClient.awaitFindAutocompletePredictions {
                    locationBias = bias
                    typesFilter = listOf(PlaceTypes.ESTABLISHMENT)
                    sessionToken = this@PlacesSearchViewModel.sessionToken
                    this.query = query
                    countries = listOf("US")
                }

                PlacesSearchEventFound(response.autocompletePredictions)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                PlacesSearchEventError(e)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PlacesSearchEventIdle
        )

    private val _transientError = MutableStateFlow<String?>(null)
    val transientError: StateFlow<String?> = _transientError

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onAutocompletePredictionClicked(prediction: AutocompletePrediction) {
        viewModelScope.launch {
            try {
                val currentToken = sessionToken
                // Using the official SDK-provided awaitFetchPlace extension
                val response = placesClient.awaitFetchPlace(
                    prediction.placeId,
                    listOf(
                        Place.Field.DISPLAY_NAME,
                        Place.Field.FORMATTED_ADDRESS,
                        Place.Field.LOCATION,
                        Place.Field.BUSINESS_STATUS
                    )
                ) {
                    sessionToken = currentToken
                }

                Log.d("PlacesSearchViewModel", "Got place ${response.place}")
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("PlacesSearchViewModel", "Error fetching place details", e)
                _transientError.value = "Failed to fetch details for ${prediction.getPrimaryText(null)}"
            } finally {
                // Success or failure! Reset session token after a selection to start a new billing session
                sessionToken = null
            }
        }
    }

    fun clearTransientError() {
        _transientError.value = null
    }
}
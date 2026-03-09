// Copyright 2026 Google LLC
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

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.kotlin.awaitFetchPlace
import com.google.android.libraries.places.api.net.kotlin.awaitFetchResolvedPhotoUri
import com.google.android.libraries.places.api.net.kotlin.awaitFindAutocompletePredictions
import com.google.android.libraries.places.api.net.kotlin.awaitSearchNearby
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
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.PhotoMetadata
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

/**
 * State definitions for the photo fetching demo search flow.
 */
sealed interface PhotoDemoEvent
object PhotoDemoEventIdle : PhotoDemoEvent
object PhotoDemoEventLoading : PhotoDemoEvent
data class PhotoDemoEventResults(val predictions: List<AutocompletePrediction>) : PhotoDemoEvent
data class PhotoDemoEventError(val exception: Exception) : PhotoDemoEvent

/**
 * State for the photo resolution and display phase.
 */
data class PhotoState(
    val uri: Uri? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for the Places Photo Demo.
 *
 * This ViewModel manages two main flows:
 * 1. An reactive search flow using [searchResults] which debounces user input and fetches predictions.
 * 2. A manual photo resolution flow triggered by [onPredictionClicked].
 *
 * It showcases the use of Places KTX suspending extensions like [awaitFindAutocompletePredictions],
 * [awaitFetchPlace], and [awaitFetchResolvedPhotoUri].
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class PlacesPhotoViewModel @Inject constructor(
    private val placesClient: PlacesClient
) : ViewModel() {

    // Internal state for the search query, used to drive the searchResults flow.
    private val _searchQuery = MutableStateFlow("")
    
    // An autocomplete session token used to group multiple requests into a single billing session.
    private var sessionToken: AutocompleteSessionToken? = null

    // State for the photo fetching phase.
    private val _photoState = MutableStateFlow(PhotoState())
    val photoState: StateFlow<PhotoState> = _photoState

    /**
     * A [StateFlow] exposing the search results based on the current query.
     *
     * This flow uses [debounce] to avoid flooding the API while the user is typing,
     * and [mapLatest] to ensure that if a new search starts, the previous one is cancelled.
     */
    val searchResults: StateFlow<PhotoDemoEvent> = _searchQuery
        .debounce(300)
        .distinctUntilChanged()
        .mapLatest { query ->
            if (query.isBlank()) return@mapLatest PhotoDemoEventIdle

            try {
                // Initialize a session token if it doesn't exist for this specific search session.
                if (sessionToken == null) {
                    sessionToken = AutocompleteSessionToken.newInstance()
                }

                // Call the Places KTX suspending extension for autocomplete.
                val response = placesClient.awaitFindAutocompletePredictions {
                    sessionToken = this@PlacesPhotoViewModel.sessionToken
                    this.query = query
                }
                PhotoDemoEventResults(response.autocompletePredictions)
            } catch (e: Exception) {
                // Standard coroutine cancellation must be propagated.
                if (e is CancellationException) throw e
                PhotoDemoEventError(e)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PhotoDemoEventIdle
        )

    /**
     * Updates the current search query and resets the photo state.
     */
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        // Reset photo state when starting a new search or clearing the query.
        _photoState.value = PhotoState()
    }

    /**
     * Triggers the photo resolution flow for a specific prediction.
     *
     * This method demonstrates the multi-step process required to get a photo URI:
     * 1. Fetch place details (specifically [Place.Field.PHOTO_METADATAS]).
     * 2. Use the metadata to request a resolved photo URI.
     *
     * @param prediction The selected autocomplete prediction.
     */
    fun onPredictionClicked(prediction: AutocompletePrediction) {
        viewModelScope.launch {
            _photoState.value = PhotoState(isLoading = true)
            try {
                val currentToken = sessionToken
                // 1. Fetch place details to get photo metadata.
                // We request only the PHOTO_METADATAS field to minimize data usage.
                val placeResponse = placesClient.awaitFetchPlace(
                    prediction.placeId,
                    listOf(Place.Field.PHOTO_METADATAS)
                ) {
                    sessionToken = currentToken
                }

                val metadata = placeResponse.place.photoMetadatas?.firstOrNull()
                if (metadata == null) {
                    _photoState.value = PhotoState(error = "No photo metadata found for this place.")
                    return@launch
                }

                // 2. Fetch the resolved photo URI using the new KTX extension.
                // This API returns a Uri that can be directly used by image loading libraries like Coil.
                val photoResponse = placesClient.awaitFetchResolvedPhotoUri(metadata)
                _photoState.value = PhotoState(uri = photoResponse.uri)

                Log.d("PlacesPhotoViewModel", "Successfully fetched photo URI: ${photoResponse.uri}")
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("PlacesPhotoViewModel", "Error fetching photo", e)
                _photoState.value = PhotoState(error = "Failed to fetch photo: ${e.message}")
            } finally {
                // End the billing session by clearing the token after the final detail request.
                sessionToken = null
            }
        }
    }

    /**
     * Demonstrates the use of [com.google.android.libraries.places.api.net.kotlin.awaitSearchNearby] 
     * as a replacement for the removed [PlacesClient.findCurrentPlace] API.
     *
     * This implementation uses a fixed location (Googleplex) for demonstration purposes.
     * In a real application, you would pass the user's current location here.
     */
    fun searchNearby() {
        viewModelScope.launch {
            _searchQuery.value = "" // Clear textual search when doing nearby search
            _photoState.value = PhotoState(isLoading = true)
            
            try {
                // Define a location restriction (e.g., 500m around Googleplex)
                val googleplex = LatLng(37.4220656, -122.0840897)
                val locationRestriction = CircularBounds.newInstance(googleplex, 500.0)
                
                // Call the Places KTX suspending extension for SearchNearby.
                // We request only the ID and PHOTO_METADATAS fields.
                val response = placesClient.awaitSearchNearby(
                    locationRestriction,
                    listOf(Place.Field.ID, Place.Field.PHOTO_METADATAS)
                ) {
                    // Optional: filter by types or adjust other parameters
                    maxResultCount = 10
                }

                // For the demo, we take the first place found that has a photo.
                val placeWithPhoto: Place? = response.places.firstOrNull { place: Place -> 
                    (place.photoMetadatas?.size ?: 0) > 0 
                }
                val metadata: PhotoMetadata? = placeWithPhoto?.photoMetadatas?.firstOrNull()

                if (metadata == null) {
                    _photoState.value = PhotoState(error = "No nearby places with photos found.")
                    return@launch
                }

                // Resolve the photo URI
                val photoResponse = placesClient.awaitFetchResolvedPhotoUri(metadata)
                _photoState.value = PhotoState(uri = photoResponse.uri)
                
                Log.d("PlacesPhotoViewModel", "Successfully found nearby place and fetched photo URI: ${photoResponse.uri}")
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("PlacesPhotoViewModel", "Error searching nearby", e)
                _photoState.value = PhotoState(error = "Failed to search nearby: ${e.message}")
            }
        }
    }
}

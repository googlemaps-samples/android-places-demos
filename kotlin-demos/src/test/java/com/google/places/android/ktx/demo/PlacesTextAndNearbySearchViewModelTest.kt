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

import android.os.Looper
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.IsOpenResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextResponse
import com.google.android.libraries.places.api.net.SearchNearbyResponse
import com.google.android.libraries.places.api.net.kotlin.awaitIsOpen
import com.google.android.libraries.places.api.net.kotlin.awaitSearchByText
import com.google.android.libraries.places.api.net.kotlin.awaitSearchNearby
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Unit tests for [PlacesTextAndNearbySearchViewModel] verifying coroutine-based Text Search,
 * Nearby Search, KTX request DSL builders, and `isOpen` status inspection.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PlacesTextAndNearbySearchViewModelTest {

    private lateinit var placesClient: PlacesClient
    private lateinit var viewModel: PlacesTextAndNearbySearchViewModel

    @Before
    fun setUp() {
        mockkStatic("com.google.android.libraries.places.api.net.kotlin.PlacesClientKt")
        placesClient = mockk(relaxed = true)
        viewModel = PlacesTextAndNearbySearchViewModel(placesClient)
    }

    @After
    fun tearDown() {
        unmockkStatic("com.google.android.libraries.places.api.net.kotlin.PlacesClientKt")
    }

    @Test
    fun searchByText_whenQueryIsBlank_updatesErrorStateWithoutCallingClient() {
        viewModel.searchByText("   ")

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.errorMessage).isEqualTo("Please enter a non-empty text search query.")
    }

    @Test
    fun buildSearchByTextRequest_populatesKtxRequestDslProperties() {
        val request = viewModel.buildSearchByTextRequest(
            query = "Opera House",
            includedType = "performing_arts_theater",
            openNow = true
        )

        assertThat(request.textQuery).isEqualTo("Opera House")
        assertThat(request.includedType).isEqualTo("performing_arts_theater")
        assertThat(request.isOpenNow).isTrue()
        assertThat(request.placeFields)
            .containsExactlyElementsIn(PlacesTextAndNearbySearchViewModel.SEARCH_PLACE_FIELDS)
        val bias = request.locationBias as CircularBounds
        assertThat(bias.center).isEqualTo(LatLng(-33.8567844, 151.2152967))
        assertThat(bias.radius).isWithin(0.01).of(2000.0)
    }

    @Test
    fun buildSearchNearbyRequest_populatesKtxRequestDslProperties() {
        val request = viewModel.buildSearchNearbyRequest(includedType = "cafe")

        assertThat(request.includedTypes).containsExactly("cafe")
        assertThat(request.placeFields)
            .containsExactlyElementsIn(PlacesTextAndNearbySearchViewModel.SEARCH_PLACE_FIELDS)
        val restriction = request.locationRestriction as CircularBounds
        assertThat(restriction.center).isEqualTo(LatLng(-33.8567844, 151.2152967))
        assertThat(restriction.radius).isWithin(0.01).of(2000.0)
    }

    @Test
    fun searchByText_whenValidQuery_executesAwaitSearchByTextAndPopulatesPlaces() {
        val samplePlace = Place.builder()
            .setId("sydney_opera")
            .setDisplayName("Sydney Opera House")
            .setFormattedAddress("Bennelong Point, Sydney")
            .setRating(4.9)
            .build()
        coEvery {
            placesClient.awaitSearchByText(
                textQuery = "Opera House",
                placeFields = PlacesTextAndNearbySearchViewModel.SEARCH_PLACE_FIELDS,
                actions = any()
            )
        } returns SearchByTextResponse.newInstance(listOf(samplePlace))

        viewModel.searchByText(
            query = "Opera House",
            includedType = "performing_arts_theater",
            openNow = true
        )
        shadowOf(Looper.getMainLooper()).idle()

        val state = viewModel.uiState.value
        assertThat(state.errorMessage).isNull()
        assertThat(state.isLoading).isFalse()
        assertThat(state.places).containsExactly(samplePlace)
        assertThat(state.selectedPlace).isEqualTo(samplePlace)
    }

    @Test
    fun searchNearby_whenValidInputs_executesAwaitSearchNearbyAndPopulatesPlaces() {
        val samplePlace = Place.builder()
            .setId("cafe_sydney")
            .setDisplayName("Cafe Sydney")
            .build()
        coEvery {
            placesClient.awaitSearchNearby(
                locationRestriction = any(),
                placeFields = PlacesTextAndNearbySearchViewModel.SEARCH_PLACE_FIELDS,
                actions = any()
            )
        } returns SearchNearbyResponse.newInstance(listOf(samplePlace))

        viewModel.searchNearby(includedType = "cafe")
        shadowOf(Looper.getMainLooper()).idle()

        val state = viewModel.uiState.value
        assertThat(state.errorMessage).isNull()
        assertThat(state.isLoading).isFalse()
        assertThat(state.places).containsExactly(samplePlace)
        assertThat(state.selectedPlace).isEqualTo(samplePlace)
    }

    @Test
    fun selectPlaceAndCheckOpenStatus_updatesSelectedPlaceAndIsOpenStatus() {
        val samplePlace = Place.builder()
            .setId("place_open_1")
            .setDisplayName("Open Bistro")
            .build()
        coEvery {
            placesClient.awaitIsOpen("place_open_1", any())
        } returns IsOpenResponse.newInstance(true)

        viewModel.selectPlaceAndCheckOpenStatus(samplePlace)
        shadowOf(Looper.getMainLooper()).idle()

        val state = viewModel.uiState.value
        assertThat(state.errorMessage).isNull()
        assertThat(state.selectedPlace).isEqualTo(samplePlace)
        assertThat(state.isOpenStatus).isTrue()
    }
}

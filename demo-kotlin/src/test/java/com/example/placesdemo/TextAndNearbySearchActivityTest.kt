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

package com.example.placesdemo

import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.SearchByTextResponse
import com.google.android.libraries.places.api.net.SearchNearbyResponse
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Robolectric unit tests for [TextAndNearbySearchActivity] and [StringUtil] search formatters.
 *
 * ## Verification Scope
 * - Verifies that [TextAndNearbySearchActivity.buildSearchByTextRequest] maps UI inputs (text query,
 *   circular bounds location bias, included type, and open-now checkbox) into a valid
 *   [com.google.android.libraries.places.api.net.SearchByTextRequest].
 * - Verifies that [TextAndNearbySearchActivity.buildSearchNearbyRequest] constructs a valid
 *   [com.google.android.libraries.places.api.net.SearchNearbyRequest] with [CircularBounds] and
 *   included types.
 * - Verifies input validation when the query is blank or the radius is non-positive.
 * - Verifies [StringUtil.stringify] output for both [SearchByTextResponse] and [SearchNearbyResponse].
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class TextAndNearbySearchActivityTest {

    @Before
    fun setUp() {
        if (!Places.isInitialized()) {
            Places.initialize(ApplicationProvider.getApplicationContext(), "fake_api_key")
        }
    }

    @Test
    fun buildSearchByTextRequest_withDefaultInputs_populatesExpectedParameters() {
        ActivityScenario.launch(TextAndNearbySearchActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<CheckBox>(R.id.checkbox_open_now).isChecked = true

                val request = activity.buildSearchByTextRequest()

                assertThat(request).isNotNull()
                assertThat(request!!.textQuery).isEqualTo("Spicy Vegetarian Food in Sydney")
                assertThat(request.includedType).isEqualTo("restaurant")
                assertThat(request.isOpenNow).isTrue()
                assertThat(request.placeFields)
                    .containsExactlyElementsIn(TextAndNearbySearchActivity.DEFAULT_SEARCH_FIELDS)
                assertThat(request.locationBias).isInstanceOf(CircularBounds::class.java)
                val bias = request.locationBias as CircularBounds
                assertThat(bias.center).isEqualTo(LatLng(-33.8567844, 151.2152967))
                assertThat(bias.radius).isWithin(0.01).of(2000.0)
            }
        }
    }

    @Test
    fun buildSearchByTextRequest_whenQueryIsBlank_displaysErrorAndReturnsNull() {
        ActivityScenario.launch(TextAndNearbySearchActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<EditText>(R.id.input_text_query).setText("   ")

                val request = activity.buildSearchByTextRequest()

                assertThat(request).isNull()
                val responseText = activity.findViewById<TextView>(R.id.response_text)
                assertThat(responseText.text.toString())
                    .isEqualTo(activity.getString(R.string.error_empty_text_query))
            }
        }
    }

    @Test
    fun buildSearchNearbyRequest_withDefaultInputs_populatesBoundsAndIncludedTypes() {
        ActivityScenario.launch(TextAndNearbySearchActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val request = activity.buildSearchNearbyRequest()

                assertThat(request).isNotNull()
                assertThat(request!!.includedTypes).containsExactly("restaurant")
                assertThat(request.placeFields)
                    .containsExactlyElementsIn(TextAndNearbySearchActivity.DEFAULT_SEARCH_FIELDS)
                assertThat(request.locationRestriction).isInstanceOf(CircularBounds::class.java)
                val restriction = request.locationRestriction as CircularBounds
                assertThat(restriction.center).isEqualTo(LatLng(-33.8567844, 151.2152967))
                assertThat(restriction.radius).isWithin(0.01).of(2000.0)
            }
        }
    }

    @Test
    fun buildSearchNearbyRequest_whenRadiusIsZero_displaysErrorAndReturnsNull() {
        ActivityScenario.launch(TextAndNearbySearchActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<EditText>(R.id.input_radius_meters).setText("0")

                val request = activity.buildSearchNearbyRequest()

                assertThat(request).isNull()
                val responseText = activity.findViewById<TextView>(R.id.response_text)
                assertThat(responseText.text.toString())
                    .isEqualTo(activity.getString(R.string.error_invalid_radius))
            }
        }
    }

    @Test
    fun stringifySearchResponses_formatsPlaceSummary() {
        val samplePlace = Place.builder()
            .setId("place_123")
            .setDisplayName("Sydney Opera House")
            .setFormattedAddress("Bennelong Point, Sydney NSW 2000")
            .setRating(4.8)
            .build()

        val textResponse = SearchByTextResponse.newInstance(listOf(samplePlace))
        val formattedText = StringUtil.stringify(textResponse, false)
        assertThat(formattedText).contains("1 Text Search Results:")
        assertThat(formattedText).contains("Sydney Opera House (place_123)")
        assertThat(formattedText).contains("Bennelong Point, Sydney NSW 2000")
        assertThat(formattedText).contains("[Rating: 4.8]")

        val nearbyResponse = SearchNearbyResponse.newInstance(listOf(samplePlace))
        val formattedNearby = StringUtil.stringify(nearbyResponse, false)
        assertThat(formattedNearby).contains("1 Nearby Places Results:")
        assertThat(formattedNearby).contains("Sydney Opera House (place_123)")
    }
}

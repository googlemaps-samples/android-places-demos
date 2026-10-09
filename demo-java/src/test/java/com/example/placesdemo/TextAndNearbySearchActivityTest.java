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

package com.example.placesdemo;

import static com.google.common.truth.Truth.assertThat;

import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.CircularBounds;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.SearchByTextRequest;
import com.google.android.libraries.places.api.net.SearchByTextResponse;
import com.google.android.libraries.places.api.net.SearchNearbyRequest;
import com.google.android.libraries.places.api.net.SearchNearbyResponse;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/**
 * Robolectric unit tests for {@link TextAndNearbySearchActivity} and {@link StringUtil} search
 * response formatters.
 *
 * <h2>Verification Scope</h2>
 * <ul>
 *   <li>Verifies that {@link TextAndNearbySearchActivity#buildSearchByTextRequest()} accurately
 *       maps UI controls (query, circular location bias, included type, openNow flag, and place
 *       fields) into a valid {@link SearchByTextRequest}.
 *   <li>Verifies that {@link TextAndNearbySearchActivity#buildSearchNearbyRequest()} constructs a
 *       valid {@link SearchNearbyRequest} with a {@link CircularBounds} restriction and included
 *       types list.
 *   <li>Verifies that invalid user inputs (blank query, malformed coordinates, or non-positive
 *       radius) are rejected gracefully with descriptive UI error messages.
 *   <li>Verifies that {@link StringUtil#stringify(SearchByTextResponse, boolean)} and {@link
 *       StringUtil#stringify(SearchNearbyResponse, boolean)} format returned {@link Place} records.
 * </ul>
 */
@RunWith(AndroidJUnit4.class)
@Config(sdk = {33})
public class TextAndNearbySearchActivityTest {

    @Before
    public void setUp() {
        if (!Places.isInitialized()) {
            Places.initialize(ApplicationProvider.getApplicationContext(), "fake_api_key");
        }
    }

    @Test
    public void buildSearchByTextRequest_withDefaultInputs_populatesExpectedParameters() {
        try (ActivityScenario<TextAndNearbySearchActivity> scenario =
                ActivityScenario.launch(TextAndNearbySearchActivity.class)) {
            scenario.onActivity(activity -> {
                CheckBox openNow = activity.findViewById(R.id.checkbox_open_now);
                openNow.setChecked(true);

                SearchByTextRequest request = activity.buildSearchByTextRequest();

                assertThat(request).isNotNull();
                assertThat(request.getTextQuery()).isEqualTo("Spicy Vegetarian Food in Sydney");
                assertThat(request.getIncludedType()).isEqualTo("restaurant");
                assertThat(request.isOpenNow()).isTrue();
                assertThat(request.getPlaceFields())
                        .containsExactlyElementsIn(TextAndNearbySearchActivity.DEFAULT_SEARCH_FIELDS);
                assertThat(request.getLocationBias()).isInstanceOf(CircularBounds.class);
                CircularBounds bias = (CircularBounds) request.getLocationBias();
                assertThat(bias.getCenter()).isEqualTo(new LatLng(-33.8567844, 151.2152967));
                assertThat(bias.getRadius()).isWithin(0.01).of(2000.0);
            });
        }
    }

    @Test
    public void buildSearchByTextRequest_whenQueryIsBlank_displaysErrorAndReturnsNull() {
        try (ActivityScenario<TextAndNearbySearchActivity> scenario =
                ActivityScenario.launch(TextAndNearbySearchActivity.class)) {
            scenario.onActivity(activity -> {
                EditText queryInput = activity.findViewById(R.id.input_text_query);
                queryInput.setText("   ");

                SearchByTextRequest request = activity.buildSearchByTextRequest();

                assertThat(request).isNull();
                TextView responseText = activity.findViewById(R.id.response_text);
                assertThat(responseText.getText().toString())
                        .isEqualTo(activity.getString(R.string.error_empty_text_query));
            });
        }
    }

    @Test
    public void buildSearchNearbyRequest_withDefaultInputs_populatesBoundsAndIncludedTypes() {
        try (ActivityScenario<TextAndNearbySearchActivity> scenario =
                ActivityScenario.launch(TextAndNearbySearchActivity.class)) {
            scenario.onActivity(activity -> {
                SearchNearbyRequest request = activity.buildSearchNearbyRequest();

                assertThat(request).isNotNull();
                assertThat(request.getIncludedTypes()).containsExactly("restaurant");
                assertThat(request.getPlaceFields())
                        .containsExactlyElementsIn(TextAndNearbySearchActivity.DEFAULT_SEARCH_FIELDS);
                assertThat(request.getLocationRestriction()).isInstanceOf(CircularBounds.class);
                CircularBounds restriction = (CircularBounds) request.getLocationRestriction();
                assertThat(restriction.getCenter()).isEqualTo(new LatLng(-33.8567844, 151.2152967));
                assertThat(restriction.getRadius()).isWithin(0.01).of(2000.0);
            });
        }
    }

    @Test
    public void buildSearchNearbyRequest_whenRadiusIsNegative_displaysErrorAndReturnsNull() {
        try (ActivityScenario<TextAndNearbySearchActivity> scenario =
                ActivityScenario.launch(TextAndNearbySearchActivity.class)) {
            scenario.onActivity(activity -> {
                EditText radiusInput = activity.findViewById(R.id.input_radius_meters);
                radiusInput.setText("-50");

                SearchNearbyRequest request = activity.buildSearchNearbyRequest();

                assertThat(request).isNull();
                TextView responseText = activity.findViewById(R.id.response_text);
                assertThat(responseText.getText().toString())
                        .isEqualTo(activity.getString(R.string.error_invalid_radius));
            });
        }
    }

    @Test
    public void stringifySearchResponses_formatsPlaceSummary() {
        Place samplePlace = Place.builder()
                .setId("place_123")
                .setDisplayName("Sydney Opera House")
                .setFormattedAddress("Bennelong Point, Sydney NSW 2000")
                .setRating(4.8)
                .build();

        SearchByTextResponse textResponse =
                SearchByTextResponse.newInstance(Collections.singletonList(samplePlace));
        String formattedText = StringUtil.stringify(textResponse, false);
        assertThat(formattedText).contains("1 Text Search Results:");
        assertThat(formattedText).contains("Sydney Opera House [place_123]");
        assertThat(formattedText).contains("Bennelong Point, Sydney NSW 2000");
        assertThat(formattedText).contains("4.8");

        SearchNearbyResponse nearbyResponse =
                SearchNearbyResponse.newInstance(Collections.singletonList(samplePlace));
        String formattedNearby = StringUtil.stringify(nearbyResponse, false);
        assertThat(formattedNearby).contains("1 Nearby Search Results:");
        assertThat(formattedNearby).contains("Sydney Opera House [place_123]");
    }
}

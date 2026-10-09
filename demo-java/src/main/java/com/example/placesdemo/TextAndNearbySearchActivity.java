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

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import com.example.placesdemo.databinding.ActivityTextAndNearbySearchBinding;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.CircularBounds;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.net.SearchByTextRequest;
import com.google.android.libraries.places.api.net.SearchNearbyRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Activity demonstrating Places SDK (New) {@link SearchByTextRequest} and {@link SearchNearbyRequest}.
 *
 * <h2>Architectural Overview</h2>
 * <p>The Places SDK (New) provides two complementary discovery endpoints on {@link PlacesClient}:
 * <ul>
 *   <li><b>Text Search ({@link PlacesClient#searchByText(SearchByTextRequest)})</b>: Evaluates a
 *       free-form natural language query (e.g., {@code "Spicy Vegetarian Food in Sydney"}) with an
 *       optional {@link CircularBounds} location bias, single {@code includedType} filter, and
 *       {@code openNow} status constraint.
 *   <li><b>Nearby Search ({@link PlacesClient#searchNearby(SearchNearbyRequest)})</b>: Performs a
 *       strictly bounded geometric search inside a {@link CircularBounds} region, optionally
 *       filtering by a list of {@code includedTypes} and ranking by popularity or distance.
 * </ul>
 *
 * <p>Request construction is factored into package-private helper methods ({@link
 * #buildSearchByTextRequest()} and {@link #buildSearchNearbyRequest()}) so that unit tests can
 * deterministically verify request parameters without requiring live network calls.
 */
public class TextAndNearbySearchActivity extends AppCompatActivity {

    private static final double DEFAULT_RADIUS_METERS = 2000.0;
    private static final int DEFAULT_MAX_RESULTS = 10;

    @VisibleForTesting
    static final List<Place.Field> DEFAULT_SEARCH_FIELDS = Arrays.asList(
            Place.Field.ID,
            Place.Field.DISPLAY_NAME,
            Place.Field.FORMATTED_ADDRESS,
            Place.Field.LOCATION,
            Place.Field.RATING,
            Place.Field.TYPES
    );

    private PlacesClient placesClient;
    private ActivityTextAndNearbySearchBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        binding = ActivityTextAndNearbySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.topBar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        placesClient = Places.createClient(this);

        binding.searchByTextButton.setOnClickListener(v -> executeSearchByText());
        binding.searchNearbyButton.setOnClickListener(v -> executeSearchNearby());
    }

    /**
     * Validates UI inputs, builds a {@link SearchByTextRequest}, and dispatches it via {@link
     * PlacesClient#searchByText(SearchByTextRequest)}.
     */
    private void executeSearchByText() {
        SearchByTextRequest request = buildSearchByTextRequest();
        if (request == null) {
            return;
        }

        setLoading(true);
        binding.responseText.setText("Executing searchByText(\"" + request.getTextQuery() + "\")...");

        placesClient.searchByText(request)
                .addOnSuccessListener(response ->
                        binding.responseText.setText(
                                StringUtil.stringify(response, binding.displayRawResults.isChecked())))
                .addOnFailureListener(exception ->
                        binding.responseText.setText("SearchByText Error: " + exception.getMessage()))
                .addOnCompleteListener(task -> setLoading(false));
    }

    /**
     * Validates UI inputs, builds a {@link SearchNearbyRequest}, and dispatches it via {@link
     * PlacesClient#searchNearby(SearchNearbyRequest)}.
     */
    private void executeSearchNearby() {
        SearchNearbyRequest request = buildSearchNearbyRequest();
        if (request == null) {
            return;
        }

        setLoading(true);
        binding.responseText.setText("Executing searchNearby...");

        placesClient.searchNearby(request)
                .addOnSuccessListener(response ->
                        binding.responseText.setText(
                                StringUtil.stringify(response, binding.displayRawResults.isChecked())))
                .addOnFailureListener(exception ->
                        binding.responseText.setText("SearchNearby Error: " + exception.getMessage()))
                .addOnCompleteListener(task -> setLoading(false));
    }

    /**
     * Constructs a {@link SearchByTextRequest} from the current UI controls, or returns {@code null}
     * after displaying an error message if required inputs are invalid.
     */
    @Nullable
    @VisibleForTesting
    SearchByTextRequest buildSearchByTextRequest() {
        String query = binding.inputTextQuery.getText().toString().trim();
        if (query.isEmpty()) {
            binding.responseText.setText(R.string.error_empty_text_query);
            Toast.makeText(this, R.string.error_empty_text_query, Toast.LENGTH_SHORT).show();
            return null;
        }

        CircularBounds bounds = parseCircularBounds();
        if (bounds == null) {
            return null;
        }

        SearchByTextRequest.Builder builder = SearchByTextRequest.builder(query, DEFAULT_SEARCH_FIELDS)
                .setLocationBias(bounds)
                .setOpenNow(binding.checkboxOpenNow.isChecked())
                .setMaxResultCount(DEFAULT_MAX_RESULTS)
                .setRankPreference(SearchByTextRequest.RankPreference.RELEVANCE);

        String includedType = binding.inputIncludedType.getText().toString().trim();
        if (!includedType.isEmpty()) {
            builder.setIncludedType(includedType);
        }

        return builder.build();
    }

    /**
     * Constructs a {@link SearchNearbyRequest} from the current UI controls, or returns {@code null}
     * after displaying an error message if coordinates or radius are invalid.
     */
    @Nullable
    @VisibleForTesting
    SearchNearbyRequest buildSearchNearbyRequest() {
        CircularBounds bounds = parseCircularBounds();
        if (bounds == null) {
            return null;
        }

        SearchNearbyRequest.Builder builder = SearchNearbyRequest.builder(bounds, DEFAULT_SEARCH_FIELDS)
                .setMaxResultCount(DEFAULT_MAX_RESULTS)
                .setRankPreference(SearchNearbyRequest.RankPreference.POPULARITY);

        String includedType = binding.inputIncludedType.getText().toString().trim();
        if (!includedType.isEmpty()) {
            builder.setIncludedTypes(Collections.singletonList(includedType));
        }

        return builder.build();
    }

    @Nullable
    private CircularBounds parseCircularBounds() {
        LatLng center = StringUtil.convertToLatLng(binding.inputCenterLatLng.getText().toString().trim());
        if (center == null) {
            binding.responseText.setText(R.string.error_alert_message_invalid_bounds);
            return null;
        }

        double radius = DEFAULT_RADIUS_METERS;
        String radiusStr = binding.inputRadiusMeters.getText().toString().trim();
        if (!radiusStr.isEmpty()) {
            try {
                radius = Double.parseDouble(radiusStr);
                if (radius <= 0) {
                    binding.responseText.setText(R.string.error_invalid_radius);
                    return null;
                }
            } catch (NumberFormatException e) {
                binding.responseText.setText(R.string.error_invalid_radius);
                return null;
            }
        }

        return CircularBounds.newInstance(center, radius);
    }

    private void setLoading(boolean loading) {
        binding.loading.setVisibility(loading ? View.VISIBLE : View.INVISIBLE);
    }
}

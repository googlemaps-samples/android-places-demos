/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.placesdemo;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.EdgeToEdge;

import com.example.placesdemo.databinding.ActivityPlaceActionsAdvancedBinding;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AddressDescriptor;
import com.google.android.libraries.places.api.model.Area;
import com.google.android.libraries.places.api.model.ContainingPlace;
import com.google.android.libraries.places.api.model.Landmark;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PriceRange;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.model.CornerPlaceAction;
import com.google.android.libraries.places.widget.model.PlaceAction;
import com.google.android.libraries.places.widget.model.PlaceActionProvider;
import com.google.android.libraries.places.widget.model.SearchMediaOptions;
import com.google.android.libraries.places.widget.model.SearchReviewsOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Java Activity demonstrating Places SDK 5.3.0 features:
 * - AddressDescriptor (Areas and Landmarks)
 * - ContainingPlace
 * - PriceRange
 * - SearchMediaOptions and SearchReviewsOptions
 * - PlaceActionProvider / PlaceAction configuration
 */
public class PlaceActionsAndAdvancedDetailsActivity extends AppCompatActivity {

    private PlacesClient placesClient;
    private ActivityPlaceActionsAdvancedBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        binding = ActivityPlaceActionsAdvancedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.topBar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        placesClient = Places.createClient(this);

        binding.fetchAdvancedButton.setOnClickListener(v -> {
            String placeId = binding.inputPlaceId.getText().toString().trim();
            if (placeId.isEmpty()) {
                Toast.makeText(this, "Please enter a Place ID", Toast.LENGTH_SHORT).show();
                return;
            }
            fetchAdvancedDetailsAndShowActions(placeId);
        });
    }

    private void fetchAdvancedDetailsAndShowActions(String placeId) {
        binding.responseText.setText("Fetching details for Place ID: " + placeId + "...");

        List<Place.Field> placeFields = Arrays.asList(
                Place.Field.ID,
                Place.Field.DISPLAY_NAME,
                Place.Field.FORMATTED_ADDRESS,
                Place.Field.ADDRESS_DESCRIPTOR,
                Place.Field.CONTAINING_PLACES,
                Place.Field.PRICE_RANGE
        );

        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();

        placesClient.fetchPlace(request).addOnSuccessListener(response -> {
            Place place = response.getPlace();
            StringBuilder sb = new StringBuilder();
            sb.append("=== PLACE DETAILS (5.3.0) ===\n");
            sb.append("Name: ").append(place.getDisplayName()).append("\n");
            sb.append("Formatted Address: ").append(place.getFormattedAddress()).append("\n\n");

            // AddressDescriptor check
            AddressDescriptor descriptor = place.getAddressDescriptor();
            if (descriptor != null) {
                sb.append("--- AddressDescriptor ---\n");
                List<Area> areas = descriptor.getAreas();
                if (areas != null) {
                    sb.append("Areas (").append(areas.size()).append("):\n");
                    for (Area area : areas) {
                        sb.append("  * ").append(area.getDisplayName()).append(" (").append(area.getContainment()).append(")\n");
                    }
                }
                List<Landmark> landmarks = descriptor.getLandmarks();
                if (landmarks != null) {
                    sb.append("Landmarks (").append(landmarks.size()).append("):\n");
                    for (Landmark landmark : landmarks) {
                        sb.append("  * ").append(landmark.getDisplayName()).append(" (").append(landmark.getSpatialRelationship()).append(")\n");
                    }
                }
                sb.append("\n");
            } else {
                sb.append("AddressDescriptor: None available for this place\n\n");
            }

            // ContainingPlace check
            List<ContainingPlace> containingList = place.getContainingPlaces();
            if (containingList != null) {
                sb.append("--- Containing Places ---\n");
                for (ContainingPlace cp : containingList) {
                    sb.append("  * ID: ").append(cp.getId()).append(", ResourceName: ").append(cp.getResourceName()).append("\n");
                }
                sb.append("\n");
            } else {
                sb.append("Containing Places: None available for this place\n\n");
            }

            // PriceRange check
            PriceRange pr = place.getPriceRange();
            if (pr != null) {
                sb.append("--- Price Range ---\n");
                sb.append("Start: ").append(pr.getStartPrice().getUnits()).append(".").append(pr.getStartPrice().getNanos());
                sb.append(", End: ").append(pr.getEndPrice().getUnits()).append(".").append(pr.getEndPrice().getNanos()).append("\n\n");
            } else {
                sb.append("Price Range: None available for this place\n\n");
            }

            // 2. Demonstrate 5.3.0 SearchMediaOptions & SearchReviewsOptions
            SearchMediaOptions mediaOptions = SearchMediaOptions.builder()
                    .setQuery("menu")
                    .setRankPreference(SearchMediaOptions.RankPreference.MOST_RELEVANT)
                    .build();

            SearchReviewsOptions reviewsOptions = SearchReviewsOptions.builder()
                    .setQuery("service")
                    .setRankPreference(SearchReviewsOptions.RankPreference.HIGHEST_RATING)
                    .build();

            sb.append("--- 5.3.0 Search Options ---\n");
            sb.append("SearchMediaOptions: query='").append(mediaOptions.getQuery()).append("', rank=").append(mediaOptions.getRankPreference()).append("\n");
            sb.append("SearchReviewsOptions: query='").append(reviewsOptions.getQuery()).append("', rank=").append(reviewsOptions.getRankPreference()).append("\n\n");

            // 3. Demonstrate 5.3.0 PlaceActionProvider and PlaceActions
            PlaceActionProvider actionProvider = new PlaceActionProvider() {
                @NonNull
                @Override
                public List<PlaceAction> getMainPlaceActions(@NonNull Place targetPlace) {
                    return Arrays.asList(
                            PlaceAction.CALL,
                            PlaceAction.OPEN_WEBSITE,
                            PlaceAction.OPEN_DIRECTIONS,
                            PlaceAction.OPEN_IN_MAPS
                    );
                }

                @NonNull
                @Override
                public List<CornerPlaceAction> getCornerPlaceActions(@NonNull Place targetPlace) {
                    return Collections.emptyList();
                }

                @Override
                public void addPlaceActionsChangedListener(@NonNull PlaceActionProvider.OnChangedListener listener) {}

                @Override
                public void removePlaceActionsChangedListener(@NonNull PlaceActionProvider.OnChangedListener listener) {}
            };

            List<PlaceAction> mainActions = actionProvider.getMainPlaceActions(place);
            sb.append("--- 5.3.0 Configured PlaceActions (").append(mainActions.size()).append(") ---\n");
            for (int i = 0; i < mainActions.size(); i++) {
                PlaceAction action = mainActions.get(i);
                sb.append("  Action #").append(i + 1).append(": labelResId=").append(action.getLabelTextResId()).append(", style=").append(action.getButtonStyle()).append("\n");
            }

            binding.responseText.setText(sb.toString());
        }).addOnFailureListener(exception -> {
            binding.responseText.setText("Error fetching place: " + exception.getMessage());
        });
    }
}

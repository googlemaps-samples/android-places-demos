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

package com.google.android.libraries.places.samples.javaview.demos;

import com.google.android.libraries.places.samples.javaview.R;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.IsOpenRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.samples.javaview.core.Demo;
import com.google.android.libraries.places.samples.javaview.core.DemoCategory;
import com.google.android.libraries.places.samples.javaview.databinding.ActivityPlaceAttributesAndHoursBinding;
import java.util.Arrays;
import java.util.List;

/** Demo Activity showcasing Place attributes, EV charging, containing places, price range, and IsOpen API. */
@Demo(
    category = DemoCategory.PLACE_ATTRIBUTES_HOURS,
    title = "Place Attributes & Opening Hours",
    description = "Inspect IsOpen status, EV charging attributes, primary type, containing place, and price range."
)
public class PlaceAttributesAndHoursDemoActivity extends AppCompatActivity {

    private ActivityPlaceAttributesAndHoursBinding binding;
    private PlacesClient placesClient;

    private static final List<Place.Field> ATTRIBUTE_FIELDS = Arrays.asList(
        Place.Field.ID,
        Place.Field.DISPLAY_NAME,
        Place.Field.PRIMARY_TYPE,
        Place.Field.PRIMARY_TYPE_DISPLAY_NAME,
        Place.Field.PRICE_LEVEL,
        Place.Field.PRICE_RANGE,
        Place.Field.OPENING_HOURS,
        Place.Field.CURRENT_OPENING_HOURS,
        Place.Field.PLUS_CODE,
        Place.Field.CONTAINING_PLACES,
        Place.Field.EV_CHARGE_OPTIONS,
        Place.Field.ACCESSIBILITY_OPTIONS
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlaceAttributesAndHoursBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

                
        placesClient = Places.createClient(this);

        binding.btnInspectAttributes.setOnClickListener(v -> inspectAttributes());
    }

    private void inspectAttributes() {
        String placeId = binding.placeIdEditText.getText() != null ?
            binding.placeIdEditText.getText().toString().trim() : "ChIJN1t_tDeuEmsRUsoyG83frY4";

        binding.attributesOutputText.setText("Fetching Place attributes & checking IsOpen for " + placeId + "...");

        FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.newInstance(placeId, ATTRIBUTE_FIELDS);
        placesClient.fetchPlace(fetchPlaceRequest)
            .addOnSuccessListener(fetchResponse -> {
                Place place = fetchResponse.getPlace();
                StringBuilder sb = new StringBuilder();
                sb.append("Display Name: ").append(place.getDisplayName()).append("\n");
                sb.append("Primary Type: ").append(place.getPrimaryType()).append(" (").append(place.getPrimaryTypeDisplayName()).append(")\n");
                sb.append("Price Level: ").append(place.getPriceLevel()).append("\n");
                if (place.getPriceRange() != null) {
                    sb.append("Price Range: ").append(place.getPriceRange().getStartPrice()).append(" - ")
                        .append(place.getPriceRange().getEndPrice()).append("\n");
                }
                if (place.getPlusCode() != null) {
                    sb.append("Plus Code: ").append(place.getPlusCode().getGlobalCode()).append("\n");
                }
                if (place.getContainingPlaces() != null) {
                    sb.append("Containing Places count: ").append(place.getContainingPlaces().size()).append("\n");
                }
                if (place.getEvChargeOptions() != null) {
                    sb.append("EV Charge Options: ").append(place.getEvChargeOptions().getConnectorCount()).append(" connectors\n");
                }
                if (place.getAccessibilityOptions() != null) {
                    sb.append("Accessibility (Wheelchair Entrance): ").append(place.getAccessibilityOptions().getWheelchairAccessibleEntrance()).append("\n");
                }
                if (place.getOpeningHours() != null) {
                    sb.append("\nOpening Hours:\n");
                    for (String text : place.getOpeningHours().getWeekdayText()) {
                        sb.append(" - ").append(text).append("\n");
                    }
                }

                IsOpenRequest isOpenRequest = IsOpenRequest.newInstance(place);
                placesClient.isOpen(isOpenRequest)
                    .addOnSuccessListener(isOpenResponse -> {
                        sb.append("\nIsOpen status: ").append(isOpenResponse.isOpen() != null && isOpenResponse.isOpen() ? "OPEN" : "CLOSED");
                        binding.attributesOutputText.setText(sb.toString());
                    })
                    .addOnFailureListener(e -> {
                        sb.append("\nIsOpen check failed: ").append(e.getMessage());
                        binding.attributesOutputText.setText(sb.toString());
                    });
            })
            .addOnFailureListener(e -> binding.attributesOutputText.setText("FetchPlace Error: " + e.getMessage()));
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_demo_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_info) {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("ℹ️ Demo Instructions & Info")
                .setMessage("Main Point: Demonstrates isOpen(), ContainingPlace, PriceRange, OpeningHours, and AccessibilityOptions.\n\nHow to Use: Tap Check Open Status to evaluate real-time business operating hours and view location hierarchy.")
                .setPositiveButton("Got It", null)
                .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

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

package com.google.places;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.bumptech.glide.Glide;
import com.google.android.libraries.places.api.model.Place;
import com.google.places.databinding.ActivityPlacesIconBinding;

public class PlacesIconActivity extends AppCompatActivity {
    private ActivityPlacesIconBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display.
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        binding = ActivityPlacesIconBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getTitle() + " (Java)");
        }

        binding.placesIconButton.setOnClickListener(v -> {
            // In a real app, you would get a Place object from a Place Details request or similar.
            // For this snippet, we'll create a dummy Place object.
            Place place = Place.builder()
                .setIconBackgroundColor(Color.BLUE)
                .setIconMaskUrl("https://maps.gstatic.com/mapfiles/place_api/icons/v1/png_71/generic_business-71.png")
                .build();
            getPlacesIcon(place);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getPlacesIcon(Place place) {
        // [START maps_places_places_icon_and_bg_color]
        // It's recommended to retrieve the icon_background_color and icon_mask_base_uri fields from a
        // FetchPlaceRequest and pass them to the Place object.
        // Set the image view's background color to match the place's icon background color
        Integer iconBackgroundColor = place.getIconBackgroundColor();
        if (iconBackgroundColor == null) {
            iconBackgroundColor = Color.TRANSPARENT;
        }
        binding.placesIconResult.setBackgroundColor(iconBackgroundColor);

        // Fetch the icon using Glide and set the result in the image view
        Glide.with(this)
            .load(place.getIconMaskUrl())
            .into(binding.placesIconResult);
        // [END maps_places_places_icon_and_bg_color]
    }
}

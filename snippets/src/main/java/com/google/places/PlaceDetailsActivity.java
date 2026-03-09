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

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.places.data.PlaceIdProvider;
import com.google.places.databinding.ActivityPlaceDetailsBinding;
import com.google.places.kotlin.MainApplication;
import java.util.Arrays;
import java.util.List;

public class PlaceDetailsActivity extends AppCompatActivity {

    private static final String TAG = PlaceDetailsActivity.class.getSimpleName();
    private PlacesClient placesClient;
    private ActivityPlaceDetailsBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display.
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        binding = ActivityPlaceDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getTitle() + " (Java)");
        }

        placesClient = ((MainApplication) getApplication()).getPlacesClient();

        getPlaceById();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getPlaceById() {
        // [START maps_places_get_place_by_id]
        // Define a Place ID.
        final String placeId = PlaceIdProvider.getRandomPlaceId();

        // Specify the fields to return.
        final List<Place.Field> placeFields =
                Arrays.asList(
                        Place.Field.ID,
                        Place.Field.DISPLAY_NAME,
                        Place.Field.FORMATTED_ADDRESS,
                        Place.Field.LOCATION
                );

        // Construct a request object, passing the place ID and fields array.
        final FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();

            // [START maps_places_place_details_simple]
            final CharSequence name = place.getDisplayName();
            final CharSequence address = place.getFormattedAddress();
            final LatLng location = place.getLocation();
            // [END maps_places_place_details_simple]

            binding.placeName.setText(name);
            binding.placeAddress.setText(address);
            if (location != null) {
                binding.placeLocation.setText(
                        getString(R.string.place_location, location.latitude, location.longitude)
                );
            } else {
                binding.placeLocation.setText(null);
            }

            Log.i(TAG, "Place found: " + place.getDisplayName());
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException apiException) {
                final String message = getString(R.string.place_not_found, apiException.getMessage());
                binding.placeName.setText(message);
                Log.e(TAG, "Place not found: " + exception.getMessage());
                final int statusCode = apiException.getStatusCode();
                // TODO: Handle error with given status code.
            }
        });
        // [END maps_places_get_place_by_id]
    }
}
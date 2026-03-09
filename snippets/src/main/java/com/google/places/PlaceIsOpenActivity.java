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

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.IsOpenRequest;
import com.google.android.libraries.places.api.net.IsOpenResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.places.data.PlaceIdProvider;
import com.google.places.databinding.ActivityPlaceIsOpenBinding;
import com.google.places.kotlin.MainApplication;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class PlaceIsOpenActivity extends AppCompatActivity {
    private PlacesClient placesClient;
    private ActivityPlaceIsOpenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display.
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        binding = ActivityPlaceIsOpenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getTitle() + " (Java)");
        }



        placesClient = ((MainApplication) getApplication()).getPlacesClient();

        binding.isOpenByObjectButton.setOnClickListener(v -> isOpenByPlaceObject());
        binding.isOpenByIdButton.setOnClickListener(v -> isOpenByPlaceId());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Check if the place is open at the time specified in the input fields.
     * Requires a Place object that includes Place.Field.ID
     */
    @SuppressLint("SetTextI18n")
    private void isOpenByPlaceObject() {
        // [START maps_places_place_is_open]
        @NonNull
        Calendar isOpenCalendar = Calendar.getInstance();
        String placeId = PlaceIdProvider.getRandomPlaceId();
        // Specify the required fields for an isOpen request.
        List<Place.Field> placeFields = new ArrayList<>(Arrays.asList(
                Place.Field.BUSINESS_STATUS,
                Place.Field.CURRENT_OPENING_HOURS,
                Place.Field.ID,
                Place.Field.OPENING_HOURS,
                Place.Field.DISPLAY_NAME
        ));

        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);
        Task<FetchPlaceResponse> placeTask = placesClient.fetchPlace(request);

        placeTask.addOnSuccessListener(
                (placeResponse) -> {
                    Place place = placeResponse.getPlace();
                    IsOpenRequest isOpenRequest;

                    try {
                        isOpenRequest = IsOpenRequest.newInstance(place, isOpenCalendar.getTimeInMillis());
                    } catch (IllegalArgumentException e) {
                        Log.e("PlaceIsOpen", "Error: " + e.getMessage());
                        return;
                    }
                    Task<IsOpenResponse> isOpenTask = placesClient.isOpen(isOpenRequest);

                    isOpenTask.addOnSuccessListener(
                            (isOpenResponse) -> {
                                final boolean isOpen = Boolean.TRUE.equals(isOpenResponse.isOpen());
                                binding.isOpenByObjectResult.setText(getString(R.string.is_open_by_object, place.getDisplayName(), String.valueOf(isOpen)));
                                Log.d("PlaceIsOpen", "Is open by object: " + isOpen);
                            });
                    isOpenTask.addOnFailureListener(
                            (exception) -> { // also update the result text field
                                binding.isOpenByObjectResult.setText(getString(R.string.is_open_by_object, place.getDisplayName(), "Error: " + exception.getMessage()));
                                Log.e("PlaceIsOpen", "Error: " + exception.getMessage());
                            });
                });
        placeTask.addOnFailureListener(
                (exception) -> {
                    binding.isOpenByObjectResult.setText("Error: " + exception.getMessage());
                    Log.e("PlaceIsOpen", "Error: " + exception.getMessage());
                });        // [END maps_places_place_is_open]
    }

    /**
     * Check if the place is open at the time specified in the input fields.
     * Use the Place ID in the input field for the isOpenRequest.
     */
    @SuppressLint("SetTextI18n")
    private void isOpenByPlaceId() {
        // [START maps_places_id_is_open]
        @NonNull
        Calendar isOpenCalendar = Calendar.getInstance();
        String placeId = PlaceIdProvider.getRandomPlaceId();
        IsOpenRequest isOpenRequest;

        try {
            isOpenRequest = IsOpenRequest.newInstance(placeId, isOpenCalendar.getTimeInMillis());
        } catch (IllegalArgumentException e) {
            Log.e("PlaceIsOpen", "Error: " + e.getMessage());
            return;
        }

        Task<IsOpenResponse> placeTask = placesClient.isOpen(isOpenRequest);

        placeTask.addOnSuccessListener(
                (response) -> {
                    final boolean isOpen = Boolean.TRUE.equals(response.isOpen());
                    binding.isOpenByIdResult.setText(getString(R.string.is_open_by_id, String.valueOf(isOpen)));
                    Log.d("PlaceIsOpen", "Is open by ID: " + isOpen);
                });
        placeTask.addOnFailureListener((exception) -> {
            binding.isOpenByIdResult.setText(getString(R.string.is_open_by_id, "Error: " + exception.getMessage()));
            Log.e("PlaceIsOpen", "Error: " + exception.getMessage());
        });
        // [END maps_places_id_is_open]
    }
}

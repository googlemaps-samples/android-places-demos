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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.IsOpenRequest;
import com.google.android.libraries.places.api.net.IsOpenResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class PlaceIsOpenActivity extends AppCompatActivity {
    private PlacesClient placesClient;
    private Boolean isOpen;

    /**
     * Check if the place is open at the time specified in the input fields.
     * Requires a Place object that includes Place.Field.ID
     */
    @SuppressLint("SetTextI18n")
    private void isOpenByPlaceObject() {
        // [START maps_places_place_is_open]
        @NonNull
        Calendar isOpenCalendar = Calendar.getInstance();
        String placeId = "ChIJD3uTd9hx5kcR1IQvGfr8dbk";
        // Specify the required fields for an isOpen request.
        List<Place.Field> placeFields = new ArrayList<>(Arrays.asList(
                Place.Field.BUSINESS_STATUS,
                Place.Field.CURRENT_OPENING_HOURS,
                Place.Field.ID,
                Place.Field.OPENING_HOURS,
                Place.Field.UTC_OFFSET
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
                        e.printStackTrace();
                        return;
                    }
                    Task<IsOpenResponse> isOpenTask = placesClient.isOpen(isOpenRequest);

                    isOpenTask.addOnSuccessListener(
                            (isOpenResponse) -> isOpen = isOpenResponse.isOpen());
                    // [START_EXCLUDE]
                    placeTask.addOnFailureListener(
                            Throwable::printStackTrace);
                    // [END_EXCLUDE]
                });
        // [START_EXCLUDE]
        placeTask.addOnFailureListener(
                Throwable::printStackTrace);
        // [END_EXCLUDE]
        // [END maps_places_place_is_open]
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
        String placeId = "ChIJD3uTd9hx5kcR1IQvGfr8dbk";
        IsOpenRequest isOpenRequest;

        try {
            isOpenRequest = IsOpenRequest.newInstance(placeId, isOpenCalendar.getTimeInMillis());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return;
        }

        Task<IsOpenResponse> placeTask = placesClient.isOpen(isOpenRequest);

        placeTask.addOnSuccessListener(
                (response) ->
                        isOpen = response.isOpen());
        // [START_EXCLUDE]
        placeTask.addOnFailureListener(
                Throwable::printStackTrace);
        // [END_EXCLUDE]
        // [END maps_places_id_is_open]
    }
}

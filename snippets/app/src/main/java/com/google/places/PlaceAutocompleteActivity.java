// Copyright 2020 Google LLC
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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceTypes;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class PlaceAutocompleteActivity extends AppCompatActivity {

    private static final String TAG = PlaceAutocompleteActivity.class.getSimpleName();

    private PlacesClient placesClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // [START maps_places_get_started]
        // Initialize the SDK
        final String apiKey = BuildConfig.PLACES_API_KEY;
        Places.initialize(getApplicationContext(), apiKey);

        // Create a new PlacesClient instance
        PlacesClient placesClient = Places.createClient(this);
        // [END maps_places_get_started]
    }

    private void initAutocompleteSupportFragment() {
        // [START maps_places_autocomplete_support_fragment]
        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
            }


            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
        // [END maps_places_autocomplete_support_fragment]

        // [START maps_places_autocomplete_location_bias]
        autocompleteFragment.setLocationBias(RectangularBounds.newInstance(
                new LatLng(-33.880490, 151.184363),
                new LatLng(-33.858754, 151.229596)));
        // [END maps_places_autocomplete_location_bias]

        // [START maps_places_autocomplete_location_restriction]
        autocompleteFragment.setLocationRestriction(RectangularBounds.newInstance(
                new LatLng(-33.880490, 151.184363),
                new LatLng(-33.858754, 151.229596)));
        // [END maps_places_autocomplete_location_restriction]

        // [START maps_places_autocomplete_type_filter]
        autocompleteFragment.setTypesFilter(Arrays.asList(PlaceTypes.ADDRESS, PlaceTypes.ESTABLISHMENT));
        // [END maps_places_autocomplete_type_filter]

        // [START maps_places_autocomplete_type_filter_multiple]
        autocompleteFragment.setTypesFilter(Arrays.asList("landmark", "restaurant", "store"));
        // [END maps_places_autocomplete_type_filter_multiple]

        List<Place.Field> fields = new ArrayList<>();
        // [START maps_places_intent_type_filter]
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields)
                .setTypesFilter(Arrays.asList(PlaceTypes.ADDRESS))
                .build(this);
        // [END maps_places_intent_type_filter]

        // [START maps_places_autocomplete_country_filter]
        autocompleteFragment.setCountries("AU", "NZ");
        // [END maps_places_autocomplete_country_filter]
    }

    // [START maps_places_autocomplete_intent]

    // [START_EXCLUDE silent]
    private void startAutocompleteIntent() {
        // [END_EXCLUDE]
        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);

        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this);
        startAutocomplete.launch(intent);
        // [END maps_places_autocomplete_intent]
    }

    // [START maps_places_on_activity_result]
    private final ActivityResultLauncher<Intent> startAutocomplete = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = result.getData();
                    if (intent != null) {
                        Place place = Autocomplete.getPlaceFromIntent(intent);
                        Log.i(TAG, "Place: ${place.getName()}, ${place.getId()}");
                    }
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    // The user canceled the operation.
                    Log.i(TAG, "User canceled autocomplete");
                }
            });
    // [END maps_places_on_activity_result]

    private void programmaticPlacePredictions(String query) {
        // [START maps_places_programmatic_place_predictions]
        // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
        // and once again when the user makes a selection (for example when calling fetchPlace()).
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        // Create a RectangularBounds object.
        RectangularBounds bounds = RectangularBounds.newInstance(
                new LatLng(-33.880490, 151.184363),
                new LatLng(-33.858754, 151.229596));
        // Use the builder to create a FindAutocompletePredictionsRequest.
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                // Call either setLocationBias() OR setLocationRestriction().
                .setLocationBias(bounds)
                //.setLocationRestriction(bounds)
                .setOrigin(new LatLng(-33.8749937, 151.2041382))
                .setCountries("AU", "NZ")
                .setTypesFilter(Arrays.asList(PlaceTypes.ADDRESS))
                .setSessionToken(token)
                .setQuery(query)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                Log.i(TAG, prediction.getPlaceId());
                Log.i(TAG, prediction.getPrimaryText(null).toString());
            }
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Place not found: " + apiException.getStatusCode());
            }
        });
        // [END maps_places_programmatic_place_predictions]
    }
}

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
import android.view.MenuItem;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import com.example.common_ui.catalog.Complexity;
import com.example.common_ui.catalog.Framework;
import com.example.common_ui.catalog.Sample;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.PlaceTypes;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.BasicPlaceAutocomplete;
import com.google.android.libraries.places.widget.PlaceAutocomplete;
import com.google.places.databinding.ActivityPlaceAutocompleteBinding;
import com.google.places.kotlin.MainApplication;
import java.util.List;

@Sample(
        id = "com.google.places.kotlin.PlaceAutocompleteActivity",
        title = "Snippet: Place Autocomplete",
        description =
                "DevSite documentation snippet for PlaceAutocomplete and programmatic prediction requests.",
        category = "Documentation Snippets",
        complexity = Complexity.SNIPPET,
        tags = {"#snippet", "#autocomplete", "#predictions"},
        apiCalls = {
            "PlaceAutocomplete.IntentBuilder().build(context)",
            "PlaceAutocomplete.getPredictionFromIntent(intent)",
            "FindAutocompletePredictionsRequest.builder()",
            "PlacesClient.findAutocompletePredictions(request)"
        },
        purpose =
                "Canonical DevSite code snippet illustrating PlaceAutocomplete intent launching and programmatic prediction queries in Kotlin and Java.",
        successCriteria =
                "• Activity launches and executes the Place Autocomplete snippet flow without error.",
        failureIndicators = "• Crash or missing resource on launch.",
        framework = Framework.JAVA_VIEWS)
public class PlaceAutocompleteActivity extends AppCompatActivity {
    private static final String TAG = PlaceAutocompleteActivity.class.getSimpleName();

    private PlacesClient placesClient;
    private ActivityPlaceAutocompleteBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display.
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        binding = ActivityPlaceAutocompleteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getTitle() + " (Java)");
        }

        placesClient = ((MainApplication) getApplication()).getPlacesClient();

        binding.autocompleteIntentButton.setOnClickListener(v -> startAutocompleteIntent());
        binding.programmaticAutocompleteButton.setOnClickListener(
                v -> programmaticPlacePredictions(binding.autocompleteQuery.getText().toString()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("unused")
    private void configureAutocompleteIntentBuilder() {
        // [START maps_places_autocomplete_support_fragment]
        // Build a BasicPlaceAutocomplete intent to allow the user to select a place.
        Intent basicAutocompleteIntent =
                new BasicPlaceAutocomplete.IntentBuilder()
                        .setCountries(List.of("AU", "NZ"))
                        .build(this);
        // [END maps_places_autocomplete_support_fragment]

        PlaceAutocomplete.IntentBuilder intentBuilder = new PlaceAutocomplete.IntentBuilder();

        if (binding.useRestrictionSwitch.isChecked()) {
            // [START maps_places_autocomplete_location_restriction]
            intentBuilder.setLocationRestriction(
                    RectangularBounds.newInstance(
                            new LatLng(-33.880490, 151.184363),
                            new LatLng(-33.858754, 151.229596)));
            // [END maps_places_autocomplete_location_restriction]
        } else {
            // [START maps_places_autocomplete_location_bias]
            intentBuilder.setLocationBias(
                    RectangularBounds.newInstance(
                            new LatLng(-33.880490, 151.184363),
                            new LatLng(-33.858754, 151.229596)));
            // [END maps_places_autocomplete_location_bias]
        }

        // [START maps_places_autocomplete_type_filter]
        intentBuilder.setTypesFilter(List.of(PlaceTypes.ESTABLISHMENT));
        // [END maps_places_autocomplete_type_filter]

        // [START maps_places_autocomplete_type_filter_multiple]
        intentBuilder.setTypesFilter(List.of("landmark", "restaurant", "store"));
        // [END maps_places_autocomplete_type_filter_multiple]

        // [START maps_places_autocomplete_country_filter]
        intentBuilder.setCountries(List.of("AU", "NZ"));
        // [END maps_places_autocomplete_country_filter]
    }

    // [START maps_places_autocomplete_intent]

    // [START_EXCLUDE silent]
    private void startAutocompleteIntent() {
        // [END_EXCLUDE]
        // Start the autocomplete intent.
        // [START maps_places_intent_type_filter]
        Intent intent =
                new PlaceAutocomplete.IntentBuilder()
                        .setTypesFilter(List.of(PlaceTypes.ESTABLISHMENT))
                        .build(this);
        // [END maps_places_intent_type_filter]
        startAutocomplete.launch(intent);
        // [END maps_places_autocomplete_intent]
    }

    // [START maps_places_on_activity_result]
    private final ActivityResultLauncher<Intent> startAutocomplete =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent intent = result.getData();
                            if (intent != null) {
                                AutocompletePrediction prediction =
                                        PlaceAutocomplete.getPredictionFromIntent(intent);
                                if (prediction != null) {
                                    binding.autocompleteResult.setText(
                                            getString(
                                                    R.string.place_selection,
                                                    prediction.getPrimaryText(null),
                                                    prediction.getPlaceId(),
                                                    prediction.getSecondaryText(null)));
                                    Log.i(
                                            TAG,
                                            "Place: "
                                                    + prediction.getPrimaryText(null)
                                                    + ", "
                                                    + prediction.getPlaceId());
                                }
                            }
                        } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                            // The user canceled the operation.
                            binding.autocompleteResult.setText(R.string.user_canceled_autocomplete);
                            Log.i(TAG, "User canceled autocomplete");
                        } else if (result.getResultCode()
                                == com.google.android.libraries.places.widget
                                        .PlaceAutocompleteActivity.RESULT_ERROR) {
                            Intent intent = result.getData();
                            if (intent != null) {
                                Status status = PlaceAutocomplete.getResultStatusFromIntent(intent);
                                if (status != null) {
                                    Log.e(TAG, "An error occurred: " + status.getStatusMessage());
                                }
                            }
                        }
                    });

    // [END maps_places_on_activity_result]

    private void programmaticPlacePredictions(String query) {
        // [START maps_places_programmatic_place_predictions]
        // Create a new token for the autocomplete session. Pass this to
        // FindAutocompletePredictionsRequest,
        // and once again when the user makes a selection (for example when calling fetchPlace()).
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        // Create a RectangularBounds object.
        RectangularBounds bounds =
                RectangularBounds.newInstance(
                        new LatLng(-33.880490, 151.184363), new LatLng(-33.858754, 151.229596));
        // Use the builder to create a FindAutocompletePredictionsRequest.
        FindAutocompletePredictionsRequest request =
                FindAutocompletePredictionsRequest.builder()
                        // Call either setLocationBias() OR setLocationRestriction().
                        .setLocationBias(bounds)
                        // .setLocationRestriction(bounds)
                        .setOrigin(new LatLng(-33.8749937, 151.2041382))
                        .setCountries("AU", "NZ")
                        .setTypesFilter(List.of(PlaceTypes.ESTABLISHMENT))
                        .setSessionToken(token)
                        .setQuery(query)
                        .build();

        placesClient
                .findAutocompletePredictions(request)
                .addOnSuccessListener(
                        (response) -> {
                            StringBuilder builder = new StringBuilder();
                            for (AutocompletePrediction prediction :
                                    response.getAutocompletePredictions()) {
                                builder.append(prediction.getPrimaryText(null).toString())
                                        .append("\n");
                                Log.i(TAG, prediction.getPlaceId());
                                Log.i(TAG, prediction.getPrimaryText(null).toString());
                            }
                            binding.autocompleteResult.setText(builder.toString());
                        })
                .addOnFailureListener(
                        (exception) -> {
                            if (exception instanceof ApiException apiException) {
                                Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                                binding.autocompleteResult.setText(
                                        getString(
                                                R.string.place_not_found,
                                                apiException.getMessage()));
                            }
                        });
        // [END maps_places_programmatic_place_predictions]
    }
}

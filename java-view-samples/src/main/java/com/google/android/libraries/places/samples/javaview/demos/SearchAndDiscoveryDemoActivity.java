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

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.CircularBounds;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.net.SearchByTextRequest;
import com.google.android.libraries.places.api.net.SearchNearbyRequest;
import com.google.android.libraries.places.samples.javaview.core.Demo;
import com.google.android.libraries.places.samples.javaview.core.DemoCategory;
import com.google.android.libraries.places.samples.javaview.core.LocationHelper;
import com.google.android.libraries.places.samples.javaview.databinding.ActivitySearchAndDiscoveryBinding;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import java.util.Arrays;
import java.util.List;

/** Demo Activity showcasing SearchByText, SearchNearby, FindCurrentPlace, and Autocomplete features. */
@Demo(
    category = DemoCategory.SEARCH_DISCOVERY,
    title = "Search & Discovery",
    description = "Execute SearchByText, SearchNearby, FindCurrentPlace, and Autocomplete overlays."
)
public class SearchAndDiscoveryDemoActivity extends AppCompatActivity {

    private ActivitySearchAndDiscoveryBinding binding;
    private PlacesClient placesClient;
    private ActivityResultLauncher<Intent> autocompleteLauncher;
    private ActivityResultLauncher<String> locationPermissionLauncher;

    private static final List<Place.Field> SEARCH_FIELDS = Arrays.asList(
        Place.Field.ID,
        Place.Field.DISPLAY_NAME,
        Place.Field.FORMATTED_ADDRESS,
        Place.Field.LOCATION,
        Place.Field.RATING
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchAndDiscoveryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

                
        placesClient = Places.createClient(this);

        ArrayAdapter<LocationHelper.PresetLocation> locationAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_dropdown_item, LocationHelper.PRESETS);
        binding.locationSpinner.setAdapter(locationAdapter);

        autocompleteLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Place place = Autocomplete.getPlaceFromIntent(result.getData());
                    appendLog("Autocomplete Selected Place: " + place.getDisplayName() + " (" + place.getId() + ")");
                } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR && result.getData() != null) {
                    Status status = Autocomplete.getStatusFromIntent(result.getData());
                    appendLog("Autocomplete Error: " + status.getStatusMessage());
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    appendLog("Autocomplete Canceled.");
                }
            }
        );

        locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    appendLog("Location permission granted. Executing findCurrentPlace()...");
                    executeFindCurrentPlace();
                } else {
                    appendLog("Location permission denied. Cannot execute findCurrentPlace().");
                }
            }
        );

        binding.btnSearchByText.setOnClickListener(v -> performSearchByText());
        binding.btnSearchNearby.setOnClickListener(v -> performSearchNearby());
        binding.btnFindCurrentPlace.setOnClickListener(v -> performFindCurrentPlace());
        binding.btnAutocomplete.setOnClickListener(v -> launchAutocomplete());
    }

    private LatLng getSelectedLocation() {
        LocationHelper.PresetLocation selected = (LocationHelper.PresetLocation) binding.locationSpinner.getSelectedItem();
        return selected != null ? selected.getLatLng() : LocationHelper.GOOGLEPLEX.getLatLng();
    }

    private void performSearchByText() {
        String query = binding.queryEditText.getText() != null ? binding.queryEditText.getText().toString() : "coffee";
        LatLng center = getSelectedLocation();

        SearchByTextRequest request = SearchByTextRequest.builder(query, SEARCH_FIELDS)
            .setLocationBias(CircularBounds.newInstance(center, 5000.0))
            .setMaxResultCount(5)
            .build();

        appendLog("Executing searchByText query='" + query + "' near " + center + "...");
        placesClient.searchByText(request)
            .addOnSuccessListener(response -> {
                appendLog("SearchByText Success (" + response.getPlaces().size() + " places):");
                for (Place place : response.getPlaces()) {
                    appendLog(" - " + place.getDisplayName() + " | Address: " + place.getFormattedAddress());
                }
            })
            .addOnFailureListener(e -> appendLog("SearchByText Error: " + e.getMessage()));
    }

    private void performSearchNearby() {
        LatLng center = getSelectedLocation();
        CircularBounds bounds = CircularBounds.newInstance(center, 1000.0);

        SearchNearbyRequest request = SearchNearbyRequest.builder(bounds, SEARCH_FIELDS)
            .setMaxResultCount(5)
            .build();

        appendLog("Executing searchNearby within 1000m of " + center + "...");
        placesClient.searchNearby(request)
            .addOnSuccessListener(response -> {
                appendLog("SearchNearby Success (" + response.getPlaces().size() + " places):");
                for (Place place : response.getPlaces()) {
                    appendLog(" - " + place.getDisplayName() + " | Location: " + place.getLocation());
                }
            })
            .addOnFailureListener(e -> appendLog("SearchNearby Error: " + e.getMessage()));
    }

    private void performFindCurrentPlace() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            appendLog("Location permission missing. Requesting ACCESS_FINE_LOCATION...");
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }
        executeFindCurrentPlace();
    }

    private void executeFindCurrentPlace() {
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(SEARCH_FIELDS);
        appendLog("Executing findCurrentPlace()...");
        try {
            placesClient.findCurrentPlace(request)
                .addOnSuccessListener(response -> {
                    appendLog("FindCurrentPlace Success (" + response.getPlaceLikelihoods().size() + " likelihoods):");
                    response.getPlaceLikelihoods().stream().limit(5).forEach(lh ->
                        appendLog(" - " + lh.getPlace().getDisplayName() + " (Likelihood: " + lh.getLikelihood() + ")")
                    );
                })
                .addOnFailureListener(e -> appendLog("FindCurrentPlace Error: " + e.getMessage()));
        } catch (SecurityException e) {
            appendLog("FindCurrentPlace SecurityException: " + e.getMessage());
        }
    }

    private void launchAutocomplete() {
        String query = binding.queryEditText.getText() != null ? binding.queryEditText.getText().toString() : "coffee";
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, SEARCH_FIELDS)
            .setInitialQuery(query)
            .setLocationBias(CircularBounds.newInstance(getSelectedLocation(), 5000.0))
            .build(this);
        autocompleteLauncher.launch(intent);
    }

    private void appendLog(String message) {
        String existing = binding.logText.getText() != null ? binding.logText.getText().toString() : "";
        String newLog = existing.isEmpty() ? message : existing + "\n" + message;
        binding.logText.setText(newLog);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_demo_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_reset_defaults) {
            getSharedPreferences("demo_prefs", MODE_PRIVATE).edit().clear().apply();
            resetInputsToDefault();
            Toast.makeText(this, "Reset to factory defaults", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.action_info) {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("ℹ️ Demo Instructions & Info")
                .setMessage("Main Point: Demonstrates searchByText, searchNearby, findCurrentPlace, and Autocomplete predictions.\n\nHow to Use: Tap category chips or search input to execute live queries. Current Place automatically requests location permission or uses preset fallbacks.")
                .setPositiveButton("Got It", null)
                .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void resetInputsToDefault() {
        getSharedPreferences("demo_prefs", MODE_PRIVATE).edit().clear().apply();
    }
}

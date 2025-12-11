/*
 * Copyright 2024 Google LLC
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

import com.example.placesdemo.databinding.CurrentPlaceActivityBinding;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place.Field;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.List;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_WIFI_STATE;

import androidx.activity.EdgeToEdge;

/**
 * Activity to demonstrate {@link PlacesClient#findCurrentPlace(FindCurrentPlaceRequest)}.
 */
public class CurrentPlaceActivity extends AppCompatActivity {

    private static final String TAG = "CURRENT_PLACE";

    private PlacesClient placesClient;
    private FieldSelector fieldSelector;

    private CurrentPlaceActivityBinding binding;

    // [START maps_solutions_android_permission_request]
    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher, as an instance variable.
    @SuppressLint("MissingPermission")
    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
                if (Boolean.TRUE.equals(isGranted.get(permission.ACCESS_FINE_LOCATION))
                        && Boolean.TRUE.equals(isGranted.get(ACCESS_WIFI_STATE))) {
                    findCurrentPlaceWithPermissions();
                } else {
                    // Fallback behavior if user denies permission
                    Log.d(TAG, "User denied permission");
                }
            });
    // [END maps_solutions_android_permission_request]

    // [START maps_solutions_android_location_permissions]
    @SuppressLint("MissingPermission")
    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            findCurrentPlaceWithPermissions();
        } else {
            requestPermissionLauncher.launch(new String[]{permission.ACCESS_FINE_LOCATION, permission.ACCESS_WIFI_STATE});
        }
    }
    // [END maps_solutions_android_location_permissions]

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Enable edge-to-edge display. This must be called before calling super.onCreate().
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);

        binding = CurrentPlaceActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Retrieve a PlacesClient (previously initialized - see MainActivity)
        placesClient = Places.createClient(this);

        // Set view objects
        List<Field> placeFields = FieldSelector.allExcept(
                Field.ADDRESS_COMPONENTS,
                Field.CURBSIDE_PICKUP,
                Field.CURRENT_OPENING_HOURS,
                Field.DELIVERY,
                Field.DINE_IN,
                Field.EDITORIAL_SUMMARY,
                Field.INTERNATIONAL_PHONE_NUMBER,
                Field.OPENING_HOURS,
                Field.RESERVABLE,
                Field.SECONDARY_OPENING_HOURS,
                Field.SERVES_BEER,
                Field.SERVES_BREAKFAST,
                Field.SERVES_BRUNCH,
                Field.SERVES_DINNER,
                Field.SERVES_LUNCH,
                Field.SERVES_VEGETARIAN_FOOD,
                Field.SERVES_WINE,
                Field.TAKEOUT,
                Field.UTC_OFFSET,
                Field.WEBSITE_URI
        );
        fieldSelector = new FieldSelector(
                binding.useCustomFields,
                binding.customFieldsList,
                placeFields,
                savedInstanceState);
        setLoading(false);

        // Set listeners for programmatic Find Current Place
        binding.findCurrentPlaceButton.setOnClickListener((view) -> checkLocationPermissions());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        fieldSelector.onSaveInstanceState(bundle);
    }

    /**
     * Fetches a list of {@link PlaceLikelihood} instances that represent the Places the user is
     * most
     * likely to be at currently.
     */
    @RequiresPermission(allOf = {ACCESS_FINE_LOCATION, ACCESS_WIFI_STATE})
    private void findCurrentPlaceWithPermissions() {
        setLoading(true);

        FindCurrentPlaceRequest currentPlaceRequest =
                FindCurrentPlaceRequest.newInstance(getPlaceFields());
        Task<FindCurrentPlaceResponse> currentPlaceTask =
                placesClient.findCurrentPlace(currentPlaceRequest);

        currentPlaceTask.addOnSuccessListener(
                (response) ->
                        binding.response.setText(StringUtil.stringify(response, isDisplayRawResultsChecked())));

        currentPlaceTask.addOnFailureListener(
                (exception) -> {
                    exception.printStackTrace();
                    binding.response.setText(exception.getMessage());
                });

        currentPlaceTask.addOnCompleteListener(task -> setLoading(false));
    }

    //////////////////////////
    // Helper methods below //
    //////////////////////////

    private List<Field> getPlaceFields() {
        if (binding.useCustomFields.isChecked()) {
            return fieldSelector.getSelectedFields();
        } else {
            return fieldSelector.getAllFields();
        }
    }

    private boolean isDisplayRawResultsChecked() {
        return binding.displayRawResults.isChecked();
    }

    private void setLoading(boolean loading) {
        binding.loading.setVisibility(loading ? View.VISIBLE : View.INVISIBLE);
    }
}

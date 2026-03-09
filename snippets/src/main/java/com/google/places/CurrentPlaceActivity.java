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

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.LocationRestriction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.net.SearchNearbyRequest;
import com.google.places.databinding.ActivityCurrentPlaceBinding;
import com.google.places.databinding.ListItemPlaceBinding;
import com.google.places.kotlin.MainApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class CurrentPlaceActivity extends AppCompatActivity {

    private ActivityCurrentPlaceBinding binding;
    private PlacesClient placesClient;
    private PlacesAdapter adapter;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    findCurrentPlace();
                } else {
                    // Handle permission denied
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivityCurrentPlaceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getTitle() + " (Java)");
        }

        placesClient = ((MainApplication) getApplication()).getPlacesClient();
        binding.currentPlaceButton.setOnClickListener(v -> findCurrentPlace());

        // Set up the RecyclerView
        adapter = new PlacesAdapter();
        binding.placesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.placesRecyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void findCurrentPlace() {
        binding.progressBar.setVisibility(View.VISIBLE);

        // [START maps_places_current_place]
        // Use fields to define the data types to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.DISPLAY_NAME, Place.Field.FORMATTED_ADDRESS, Place.Field.LOCATION);

        // Use the builder to create a FindCurrentPlaceRequest.
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

        // Call findCurrentPlace and handle the response (first check that the user has granted permission).
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
            placeResponse.addOnCompleteListener(task -> {
                binding.progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    FindCurrentPlaceResponse response = task.getResult();
                    adapter.setPlaceLikelihoods(response.getPlaceLikelihoods());
                } else {
                    Exception exception = task.getException();
                    if (exception instanceof ApiException apiException) {
                        Log.e("CurrentPlace", "Place not found: " + apiException.getStatusCode());
                    }
                }
            });
        } else {
            // [START_EXCLUDE silent]
            binding.progressBar.setVisibility(View.GONE);
            // [END_EXCLUDE]
            // A local method to request required permissions;
            // See https://developer.android.com/training/permissions/requesting
            getLocationPermission();
        }
        // [END maps_places_current_place]
    }

    private void getLocationPermission() {
        requestPermissionLauncher.launch(ACCESS_FINE_LOCATION);
    }

    // Adapter for the RecyclerView
    private static class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.ViewHolder> {

        private List<PlaceLikelihood> placeLikelihoods = new ArrayList<>();

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ListItemPlaceBinding binding = ListItemPlaceBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PlaceLikelihood placeLikelihood = placeLikelihoods.get(position);
            holder.bind(placeLikelihood);
        }

        @Override
        public int getItemCount() {
            return placeLikelihoods.size();
        }

        public void setPlaceLikelihoods(List<PlaceLikelihood> placeLikelihoods) {
            this.placeLikelihoods = placeLikelihoods;
            notifyDataSetChanged();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            private final ListItemPlaceBinding binding;

            ViewHolder(ListItemPlaceBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            void bind(PlaceLikelihood placeLikelihood) {
                Place place = placeLikelihood.getPlace();
                binding.placeName.setText(place.getDisplayName());
                binding.placeAddress.setText(place.getFormattedAddress());
                binding.placeLikelihood.setText(String.format(Locale.getDefault(), "Likelihood: %.2f", placeLikelihood.getLikelihood()));
            }
        }
    }
}
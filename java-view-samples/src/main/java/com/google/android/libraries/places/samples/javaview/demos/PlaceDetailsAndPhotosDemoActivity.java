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

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AddressDescriptor;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchResolvedPhotoUriRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.samples.javaview.core.Demo;
import com.google.android.libraries.places.samples.javaview.core.DemoCategory;
import com.google.android.libraries.places.samples.javaview.databinding.ActivityPlaceDetailsAndPhotosBinding;
import java.util.Arrays;
import java.util.List;

/** Demo Activity demonstrating FetchPlace, FetchPhoto, FetchResolvedPhotoUri, and AddressDescriptor. */
@Demo(
    category = DemoCategory.PLACE_DETAILS_PHOTOS,
    title = "Place Details & Photo Resolution",
    description = "Retrieve detailed Place attributes, bitmap photos, resolved URIs, and AddressDescriptor."
)
public class PlaceDetailsAndPhotosDemoActivity extends AppCompatActivity {

    private ActivityPlaceDetailsAndPhotosBinding binding;
    private PlacesClient placesClient;
    private PhotoMetadata currentPhotoMetadata;

    private static final List<Place.Field> DETAIL_FIELDS = Arrays.asList(
        Place.Field.ID,
        Place.Field.DISPLAY_NAME,
        Place.Field.FORMATTED_ADDRESS,
        Place.Field.LOCATION,
        Place.Field.RATING,
        Place.Field.USER_RATING_COUNT,
        Place.Field.PHOTO_METADATAS,
        Place.Field.ADDRESS_DESCRIPTOR,
        Place.Field.WEBSITE_URI,
        Place.Field.NATIONAL_PHONE_NUMBER
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlaceDetailsAndPhotosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        placesClient = Places.createClient(this);

        binding.btnFetchPlace.setOnClickListener(v -> fetchPlaceDetails());
        binding.btnFetchPhoto.setOnClickListener(v -> fetchBitmapPhoto());
        binding.btnFetchResolvedUri.setOnClickListener(v -> fetchResolvedPhotoUri());
    }

    private String getPlaceId() {
        return binding.placeIdEditText.getText() != null ?
            binding.placeIdEditText.getText().toString().trim() : "ChIJN1t_tDeuEmsRUsoyG83frY4";
    }

    private void fetchPlaceDetails() {
        String placeId = getPlaceId();
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, DETAIL_FIELDS);

        binding.detailsText.setText("Fetching Place Details for " + placeId + "...");
        placesClient.fetchPlace(request)
            .addOnSuccessListener(response -> {
                Place place = response.getPlace();
                StringBuilder sb = new StringBuilder();
                sb.append("Display Name: ").append(place.getDisplayName()).append("\n");
                sb.append("Address: ").append(place.getFormattedAddress()).append("\n");
                sb.append("Location: ").append(place.getLocation()).append("\n");
                sb.append("Rating: ").append(place.getRating()).append(" (").append(place.getUserRatingCount()).append(" reviews)\n");
                sb.append("Phone: ").append(place.getNationalPhoneNumber()).append("\n");
                sb.append("Website: ").append(place.getWebsiteUri()).append("\n");

                AddressDescriptor ad = place.getAddressDescriptor();
                if (ad != null) {
                    sb.append("\nAddressDescriptor:\n");
                    if (ad.getLandmarks() != null) {
                        sb.append(" - Landmarks count: ").append(ad.getLandmarks().size()).append("\n");
                    }
                    if (ad.getAreas() != null) {
                        sb.append(" - Areas count: ").append(ad.getAreas().size()).append("\n");
                    }
                }

                if (place.getPhotoMetadatas() != null && !place.getPhotoMetadatas().isEmpty()) {
                    currentPhotoMetadata = place.getPhotoMetadatas().get(0);
                    sb.append("\nPhoto Metadatas: ").append(place.getPhotoMetadatas().size()).append(" available. First photo ready to load.");
                } else {
                    currentPhotoMetadata = null;
                    sb.append("\nNo photo metadata found.");
                }

                binding.detailsText.setText(sb.toString());
            })
            .addOnFailureListener(e -> binding.detailsText.setText("FetchPlace Error: " + e.getMessage()));
    }

    private void fetchBitmapPhoto() {
        if (currentPhotoMetadata == null) {
            binding.detailsText.setText("Please fetch Place Details first to obtain PhotoMetadata.");
            return;
        }

        FetchPhotoRequest request = FetchPhotoRequest.builder(currentPhotoMetadata)
            .setMaxWidth(800)
            .setMaxHeight(600)
            .build();

        placesClient.fetchPhoto(request)
            .addOnSuccessListener(response -> binding.photoImageView.setImageBitmap(response.getBitmap()))
            .addOnFailureListener(e -> binding.detailsText.setText("FetchPhoto Error: " + e.getMessage()));
    }

    private void fetchResolvedPhotoUri() {
        if (currentPhotoMetadata == null) {
            binding.detailsText.setText("Please fetch Place Details first to obtain PhotoMetadata.");
            return;
        }

        FetchResolvedPhotoUriRequest request = FetchResolvedPhotoUriRequest.builder(currentPhotoMetadata)
            .setMaxWidth(800)
            .setMaxHeight(600)
            .build();

        placesClient.fetchResolvedPhotoUri(request)
            .addOnSuccessListener(response -> {
                if (response.getUri() != null) {
                    Glide.with(this).load(response.getUri()).into(binding.photoImageView);
                    binding.detailsText.append("\nFetched Resolved Photo URI: " + response.getUri());
                }
            })
            .addOnFailureListener(e -> binding.detailsText.setText("FetchResolvedPhotoUri Error: " + e.getMessage()));
    }
}

/*
 * Copyright 2018 Google LLC
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

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.compat.GeoDataClient;
import com.google.android.libraries.places.compat.Place;
import com.google.android.libraries.places.compat.PlaceDetectionClient;
import com.google.android.libraries.places.compat.PlaceLikelihood;
import com.google.android.libraries.places.compat.PlaceLikelihoodBufferResponse;
import com.google.android.libraries.places.compat.PlacePhotoMetadata;
import com.google.android.libraries.places.compat.PlacePhotoMetadataBuffer;
import com.google.android.libraries.places.compat.PlacePhotoMetadataResponse;
import com.google.android.libraries.places.compat.PlacePhotoResponse;
import com.google.android.libraries.places.compat.Places;
import com.google.android.libraries.places.compat.ui.PlaceAutocomplete;
import com.google.android.libraries.places.compat.ui.PlaceAutocompleteFragment;
import com.google.android.libraries.places.compat.ui.PlaceSelectionListener;
import com.google.android.gms.tasks.Task;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.StringJoiner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {

  final int AUTOCOMPLETE_REQUEST = 2;

  // Views show text and image data returned from the Places API.
  TextView textView;
  ImageView imageView;

  GeoDataClient geoDataClient;
  PlaceDetectionClient placeDetectionClient;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    textView = findViewById(R.id.text);
    imageView = findViewById(R.id.image);
    geoDataClient = Places.getGeoDataClient(this);
    placeDetectionClient = Places.getPlaceDetectionClient(this);

    findViewById(R.id.button_current_place).setOnClickListener(v -> showCurrentPlace());
    findViewById(R.id.button_autocomplete).setOnClickListener(v -> showAutocomplete());

    setupAutoCompleteFragment();
  }

  /**
   * Shows some text and clears any previously set image.
   */
  private void showResponse(String response) {
    textView.setText(response);
    imageView.setImageResource(0);
  }

  /**
   * Shows the name of a place, and its photo.
   */
  private void showPlace(String source, Place place) {
    showResponse(String.format("%s: '%s'", source, place.getName()));
    showPhotoForPlace(place);
  }

  /**
   * Sets up the autocomplete fragment to show place details when a place is selected.
   */
  private void setupAutoCompleteFragment() {
    PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
        getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

    autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
      @Override
      public void onPlaceSelected(Place place) {
        showPlace(getString(R.string.autocomplete_fragment), place);
      }

      @Override
      public void onError(Status status) {
        showResponse("An error occurred: " + status);
      }
    });
  }

  /**
   * Shows the autocomplete activity.
   */
  private void showAutocomplete() {
    try {
      Intent intent =
          new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
              .build(this);
      startActivityForResult(intent, AUTOCOMPLETE_REQUEST);
    } catch (GooglePlayServicesRepairableException e) {
      GooglePlayServicesUtil.getErrorDialog(e.getConnectionStatusCode(), this, 0);
    } catch (GooglePlayServicesNotAvailableException e) {
      showResponse(getString(R.string.google_play_services_error));
    }
  }

  /**
   * Handles responses for autocomplete, by showing details of the place returned.
   */
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    Place place;
    String source;
    if (resultCode == RESULT_OK) {
      if (requestCode == AUTOCOMPLETE_REQUEST) {
        place = PlaceAutocomplete.getPlace(this, data);
        source = getString(R.string.autocomplete);
      } else {
        return;
      }
      showPlace(source, place);
    }
  }

  /**
   * Shows a photo for the given place, if available.
   */
  private void showPhotoForPlace(Place place) {
    Task<PlacePhotoMetadataResponse> metaData = geoDataClient.getPlacePhotos(place.getId());
    metaData.addOnCompleteListener((Task<PlacePhotoMetadataResponse> metaTask) -> {

      PlacePhotoMetadataResponse photos = metaTask.getResult();
      PlacePhotoMetadataBuffer photosBuffer = photos.getPhotoMetadata();
      if (photosBuffer.getCount() == 0) {
        return;
      }
      PlacePhotoMetadata photoMetadata = photosBuffer.get(0);

      Task<PlacePhotoResponse> photoResponse = geoDataClient.getPhoto(photoMetadata);
      photoResponse.addOnCompleteListener((Task<PlacePhotoResponse> photoTask) -> {
        PlacePhotoResponse photo = photoTask.getResult();
        imageView.setImageBitmap(photo.getBitmap());
      });
    });
  }

  /**
   * Shows a list of potential places for the device's current location, and their likelihoods.
   */
  private void showCurrentPlace() {
    int permission = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION);
    if (permission != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 0);
      return;
    }

    Task<PlaceLikelihoodBufferResponse> placeResult = placeDetectionClient.getCurrentPlace(null);

    placeResult.addOnCompleteListener((Task<PlaceLikelihoodBufferResponse> task) -> {
      PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
      StringJoiner response = new StringJoiner("\n");
      for (PlaceLikelihood likelihood : likelyPlaces) {
        response.add(String.format("Current Place '%s' has likelihood: %g",
            likelihood.getPlace().getName(),
            likelihood.getLikelihood()));
      }
      showResponse(response.toString());
      likelyPlaces.release();
    });
  }

}
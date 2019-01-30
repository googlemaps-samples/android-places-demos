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

import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.Place.Field;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity for testing {@link PlacesClient#fetchPlace(FetchPlaceRequest)}.
 */
public class PlaceAndPhotoTestActivity extends AppCompatActivity {

  private PlacesClient placesClient;
  private ImageView photoView;
  private TextView responseView;
  private FieldSelector fieldSelector;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.place_and_photo_test_activity);

    // Retrieve a PlacesClient (previously initialized - see MainActivity)
    placesClient = Places.createClient(this);

    // Set up view objects
    responseView = findViewById(R.id.response);
    photoView = findViewById(R.id.photo);
    ((CheckBox) findViewById(R.id.fetch_photo_checkbox))
        .setOnCheckedChangeListener((buttonView, isChecked) -> setPhotoSizingEnabled(isChecked));
    ((CheckBox) findViewById(R.id.use_custom_photo_reference))
        .setOnCheckedChangeListener(
            (buttonView, isChecked) -> setCustomPhotoReferenceEnabled(isChecked));
    fieldSelector =
        new FieldSelector(
            findViewById(R.id.use_custom_fields), findViewById(R.id.custom_fields_list));

    // Set listeners for programmatic Fetch Place
    findViewById(R.id.fetch_place_and_photo_button).setOnClickListener(view -> fetchPlace());

    // UI initialization
    setLoading(false);
    setPhotoSizingEnabled(false);
    setCustomPhotoReferenceEnabled(false);
  }

  /**
   * Fetches the {@link Place} specified via the UI and displays it. May also trigger {@link
   * #fetchPhoto(PhotoMetadata)} if set in the UI.
   */
  private void fetchPlace() {
    responseView.setText(null);
    photoView.setImageBitmap(null);
    dismissKeyboard(findViewById(R.id.place_id_field));

    final boolean isFetchPhotoChecked = isFetchPhotoChecked();
    List<Place.Field> placeFields = getPlaceFields();
    String customPhotoReference = getCustomPhotoReference();
    if (!validateInputs(isFetchPhotoChecked, placeFields, customPhotoReference)) {
      return;
    }

    setLoading(true);

    FetchPlaceRequest request = FetchPlaceRequest.newInstance(getPlaceId(), placeFields);
    Task<FetchPlaceResponse> placeTask = placesClient.fetchPlace(request);

    placeTask.addOnSuccessListener(
        (response) -> {
          responseView.setText(StringUtil.stringify(response, isDisplayRawResultsChecked()));
          if (isFetchPhotoChecked) {
            attemptFetchPhoto(response.getPlace());
          }
        });

    placeTask.addOnFailureListener(
        (exception) -> {
          exception.printStackTrace();
          responseView.setText(exception.getMessage());
        });

    placeTask.addOnCompleteListener(response -> setLoading(false));
  }

  private void attemptFetchPhoto(Place place) {
    List<PhotoMetadata> photoMetadatas = place.getPhotoMetadatas();
    if (photoMetadatas != null && !photoMetadatas.isEmpty()) {
      fetchPhoto(photoMetadatas.get(0));
    }
  }

  /**
   * Fetches a Bitmap using the Places API and displays it.
   *
   * @param photoMetadata from a {@link Place} instance.
   */
  private void fetchPhoto(PhotoMetadata photoMetadata) {
    photoView.setImageBitmap(null);
    setLoading(true);

    String customPhotoReference = getCustomPhotoReference();
    if (!TextUtils.isEmpty(customPhotoReference)) {
      photoMetadata = PhotoMetadata.builder(customPhotoReference).build();
    }

    FetchPhotoRequest.Builder photoRequestBuilder = FetchPhotoRequest.builder(photoMetadata);

    Integer maxWidth = readIntFromTextView(R.id.photo_max_width);
    if (maxWidth != null) {
      photoRequestBuilder.setMaxWidth(maxWidth);
    }

    Integer maxHeight = readIntFromTextView(R.id.photo_max_height);
    if (maxHeight != null) {
      photoRequestBuilder.setMaxHeight(maxHeight);
    }

    Task<FetchPhotoResponse> photoTask = placesClient.fetchPhoto(photoRequestBuilder.build());

    photoTask.addOnSuccessListener(
        response -> {
          photoView.setImageBitmap(response.getBitmap());
          StringUtil.prepend(responseView, StringUtil.stringify(response.getBitmap()));
        });

    photoTask.addOnFailureListener(
        exception -> {
          exception.printStackTrace();
          StringUtil.prepend(responseView, "Photo: " + exception.getMessage());
        });

    photoTask.addOnCompleteListener(response -> setLoading(false));
  }

  //////////////////////////
  // Helper methods below //
  //////////////////////////

  private void dismissKeyboard(EditText focusedEditText) {
    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(focusedEditText.getWindowToken(), 0);
  }

  private boolean validateInputs(
      boolean isFetchPhotoChecked, List<Field> placeFields, String customPhotoReference) {
    if (isFetchPhotoChecked) {
      if (!placeFields.contains(Field.PHOTO_METADATAS)) {
        responseView.setText(
            "'Also fetch photo?' is selected, but PHOTO_METADATAS Place Field is not.");
        return false;
      }
    } else if (!TextUtils.isEmpty(customPhotoReference)) {
      responseView.setText(
          "Using 'Custom photo reference', but 'Also fetch photo?' is not selected.");
      return false;
    }

    return true;
  }

  private String getPlaceId() {
    return ((TextView) findViewById(R.id.place_id_field)).getText().toString();
  }

  private List<Place.Field> getPlaceFields() {
    if (((CheckBox) findViewById(R.id.use_custom_fields)).isChecked()) {
      return fieldSelector.getSelectedFields();
    } else {
      return fieldSelector.getAllFields();
    }
  }

  private boolean isDisplayRawResultsChecked() {
    return ((CheckBox) findViewById(R.id.display_raw_results)).isChecked();
  }

  private boolean isFetchPhotoChecked() {
    return ((CheckBox) findViewById(R.id.fetch_photo_checkbox)).isChecked();
  }

  private String getCustomPhotoReference() {
    return ((TextView) findViewById(R.id.custom_photo_reference)).getText().toString();
  }

  private void setPhotoSizingEnabled(boolean enabled) {
    setEnabled(R.id.photo_max_width, enabled);
    setEnabled(R.id.photo_max_height, enabled);
  }

  private void setCustomPhotoReferenceEnabled(boolean enabled) {
    setEnabled(R.id.custom_photo_reference, enabled);
  }

  private void setEnabled(@IdRes int resId, boolean enabled) {
    TextView view = findViewById(resId);
    view.setEnabled(enabled);
    view.setText("");
  }

  @Nullable
  private Integer readIntFromTextView(@IdRes int resId) {
    Integer intValue = null;
    View view = findViewById(resId);

    if (view instanceof TextView) {
      CharSequence contents = ((TextView) view).getText();
      if (!TextUtils.isEmpty(contents)) {
        try {
          intValue = Integer.parseInt(contents.toString());
        } catch (NumberFormatException e) {
          showErrorAlert(R.string.error_alert_message_invalid_photo_size);
        }
      }
    }

    return intValue;
  }

  private void showErrorAlert(@StringRes int messageResId) {
    new AlertDialog.Builder(this)
        .setTitle(R.string.error_alert_title)
        .setMessage(messageResId)
        .show();
  }

  private void setLoading(boolean loading) {
    findViewById(R.id.loading).setVisibility(loading ? View.VISIBLE : View.INVISIBLE);
  }
}

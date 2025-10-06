/*
 * Copyright 2022 Google LLC
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

import com.bumptech.glide.Glide;
import com.example.placesdemo.databinding.PlaceDetailsAndPhotosActivityBinding;
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
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.activity.EdgeToEdge;

/**
 * Activity to demonstrate {@link PlacesClient#fetchPlace(FetchPlaceRequest)}.
 */
public class PlaceDetailsAndPhotosActivity extends AppCompatActivity {

    private static final String FETCHED_PHOTO_KEY = "photo_image";
    private PlacesClient placesClient;
    private PhotoMetadata photo;
    private FieldSelector fieldSelector;

    private PlaceDetailsAndPhotosActivityBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Enable edge-to-edge display. This must be called before calling super.onCreate().
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);

        binding = PlaceDetailsAndPhotosActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Retrieve a PlacesClient (previously initialized - see MainActivity)
        placesClient = Places.createClient(this);
        if (savedInstanceState != null) {
            photo = savedInstanceState.getParcelable(FETCHED_PHOTO_KEY);
        }

        binding.fetchPhotoCheckbox.setOnCheckedChangeListener(
                (buttonView, isChecked) -> setPhotoSizingEnabled(isChecked));
        binding.useCustomPhotoReference.setOnCheckedChangeListener(
                (buttonView, isChecked) -> setCustomPhotoReferenceEnabled(isChecked));
        fieldSelector =
                new FieldSelector(
                        binding.useCustomFields,
                        binding.customFieldsList,
                        savedInstanceState);

        // Set listeners for programmatic Fetch Place
        findViewById(R.id.fetch_place_and_photo_button).setOnClickListener(view -> fetchPlace());

        // UI initialization
        setLoading(false);
        setPhotoSizingEnabled(binding.fetchPhotoCheckbox.isChecked());
        setCustomPhotoReferenceEnabled(binding.useCustomPhotoReference.isChecked());
        if (photo != null) {
            fetchPhoto(photo);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        fieldSelector.onSaveInstanceState(bundle);
        bundle.putParcelable(FETCHED_PHOTO_KEY, photo);
    }

    /**
     * Fetches the {@link Place} specified via the UI and displays it. May also trigger {@link
     * #fetchPhoto(PhotoMetadata)} if set in the UI.
     */
    private void fetchPlace() {
        clearViews();

        dismissKeyboard(binding.placeIdField);

        final boolean isFetchPhotoChecked = isFetchPhotoChecked();
        final boolean isFetchIconChecked = isFetchIconChecked();
        List<Field> placeFields = getPlaceFields();
        String customPhotoReference = getCustomPhotoReference();
        if (!validateInputs(isFetchPhotoChecked, isFetchIconChecked, placeFields,
                customPhotoReference)) {
            return;
        }

        setLoading(true);

        FetchPlaceRequest request = FetchPlaceRequest.newInstance(getPlaceId(), placeFields);
        Task<FetchPlaceResponse> placeTask = placesClient.fetchPlace(request);

        placeTask.addOnSuccessListener(

                (response) -> {
                    binding.response.setText(StringUtil.stringify(response, isDisplayRawResultsChecked()));
                    if (isFetchPhotoChecked) {
                        attemptFetchPhoto(response.getPlace());
                    }
                    if (isFetchIconChecked) {
                        attemptFetchIcon(response.getPlace());
                    }
                });

        placeTask.addOnFailureListener(
                (exception) -> {
                    exception.printStackTrace();
                    binding.response.setText(exception.getMessage());
                });

        placeTask.addOnCompleteListener(response -> setLoading(false));
    }

    private void attemptFetchPhoto(Place place) {
        List<PhotoMetadata> photoMetadatas = place.getPhotoMetadatas();
        if (photoMetadatas != null && !photoMetadatas.isEmpty()) {
            fetchPhoto(photoMetadatas.get(0));
        }
    }

    private void attemptFetchIcon(Place place) {
        binding.icon.setImageBitmap(null);
        Integer bc = place.getIconBackgroundColor();
        binding.icon.setBackgroundColor(bc == null ? Color.TRANSPARENT : bc);
        String url = place.getIconMaskUrl();
        Glide.with(this).load(url).into(binding.icon);
    }

    /**
     * Fetches a Bitmap using the Places API and displays it.
     *
     * @param photoMetadata from a {@link Place} instance.
     */
    private void fetchPhoto(PhotoMetadata photoMetadata) {
        photo = photoMetadata;

        binding.photo.setImageBitmap(null);
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
                    Bitmap bitmap = response.getBitmap();
                    binding.photo.setImageBitmap(bitmap);
                    StringUtil.prepend(binding.photoMetadata, StringUtil.stringify(bitmap));
                });

        photoTask.addOnFailureListener(
                exception -> {
                    exception.printStackTrace();
                    StringUtil.prepend(binding.response, "Photo: " + exception.getMessage());
                });

        photoTask.addOnCompleteListener(response -> setLoading(false));
    }

    //////////////////////////
    // Helper methods below //
    //////////////////////////

    private void dismissKeyboard(EditText focusedEditText) {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(focusedEditText.getWindowToken(), 0);
    }

    private boolean validateInputs(boolean isFetchPhotoChecked, boolean isFetchIconChecked,
                                   List<Field> placeFields, String customPhotoReference) {
        if (isFetchPhotoChecked) {
            if (!placeFields.contains(Field.PHOTO_METADATAS)) {

                binding.response.setText(
                        "'Also fetch photo?' is selected, but PHOTO_METADATAS Place Field is not.");
                return false;
            }
        } else if (!TextUtils.isEmpty(customPhotoReference)) {
            binding.response.setText(
                    "Using 'Custom photo reference', but 'Also fetch photo?' is not selected.");
            return false;
        }
        if (isFetchIconChecked && !placeFields.contains(Field.ICON_MASK_URL)) {
            binding.response.setText(R.string.fetch_icon_missing_fields_warning);
            return false;
        }

        return true;
    }

    private String getPlaceId() {
        return ((TextView) findViewById(R.id.place_id_field)).getText().toString();
    }

    private List<Field> getPlaceFields() {
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

    private boolean isFetchIconChecked() {
        return ((CheckBox) findViewById(R.id.fetch_icon_checkbox)).isChecked();
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

    private void clearViews() {
        binding.response.setText(null);
        binding.photo.setImageBitmap(null);
        binding.photoMetadata.setText(null);
        binding.icon.setImageBitmap(null);
    }
}

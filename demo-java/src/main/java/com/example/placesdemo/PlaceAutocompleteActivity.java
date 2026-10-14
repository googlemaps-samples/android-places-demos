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

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.placesdemo.databinding.PlaceAutocompleteActivityBinding;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.LocationBias;
import com.google.android.libraries.places.api.model.LocationRestriction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.BasicPlaceAutocomplete;
import com.google.android.libraries.places.widget.PlaceAutocomplete;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Activity to demonstrate Place Autocomplete ({@link PlaceAutocomplete}, {@link
 * BasicPlaceAutocomplete}, and {@link PlacesClient#findAutocompletePredictions}).
 */
public class PlaceAutocompleteActivity extends AppCompatActivity {

    private PlacesClient placesClient;
    private FieldSelector fieldSelector;
    private PlaceAutocompleteActivityBinding binding;

    private final ActivityResultLauncher<Intent> autocompleteLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        Intent data = result.getData();
                        if (result.getResultCode() == RESULT_OK && data != null) {
                            AutocompletePrediction prediction =
                                    PlaceAutocomplete.getPredictionFromIntent(data);
                            binding.response.setText(
                                    StringUtil.stringifyAutocompletePrediction(
                                            prediction, isDisplayRawResultsChecked()));
                        } else if (result.getResultCode()
                                        == com.google.android.libraries.places.widget
                                                .PlaceAutocompleteActivity.RESULT_ERROR
                                && data != null) {
                            Status status = PlaceAutocomplete.getResultStatusFromIntent(data);
                            if (status != null) {
                                binding.response.setText(status.getStatusMessage());
                            }
                        }
                    });

    private final ActivityResultLauncher<Intent> basicAutocompleteLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        Intent data = result.getData();
                        if (result.getResultCode() == RESULT_OK && data != null) {
                            Place place = BasicPlaceAutocomplete.getPlaceFromIntent(data);
                            if (place != null) {
                                binding.response.setText(
                                        StringUtil.stringifyAutocompleteWidget(
                                                place, isDisplayRawResultsChecked()));
                            }
                        } else if (result.getResultCode()
                                        == com.google.android.libraries.places.widget
                                                .PlaceAutocompleteActivity.RESULT_ERROR
                                && data != null) {
                            Status status = BasicPlaceAutocomplete.getResultStatusFromIntent(data);
                            if (status != null) {
                                binding.response.setText(status.getStatusMessage());
                            }
                        }
                    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Enable edge-to-edge display. This must be called before calling super.onCreate().
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);

        binding = PlaceAutocompleteActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Retrieve a PlacesClient (previously initialized - see MainActivity)
        placesClient = Places.createClient(this);

        // Set up view objects
        binding.autocompleteUseTypesFilterCheckbox.setOnCheckedChangeListener(
                (buttonView, isChecked) ->
                        binding.autocompleteTypesFilterEdittext.setEnabled(isChecked));
        fieldSelector =
                new FieldSelector(
                        binding.useCustomFields, binding.customFieldsList, savedInstanceState);

        // Set listeners for Autocomplete activity
        binding.autocompleteActivityButton.setOnClickListener(view -> startAutocompleteActivity());
        binding.basicAutocompleteActivityButton.setOnClickListener(
                view -> startBasicAutocompleteActivity());

        // Set listeners for programmatic Autocomplete
        binding.fetchAutocompletePredictionsButton.setOnClickListener(
                view -> findAutocompletePredictions());

        // UI initialization
        setLoading(false);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        fieldSelector.onSaveInstanceState(bundle);
    }

    private void startAutocompleteActivity() {
        Intent autocompleteIntent =
                new PlaceAutocomplete.IntentBuilder()
                        .setInitialQuery(getQuery())
                        .setCountries(getCountries())
                        .setLocationBias(getLocationBias())
                        .setLocationRestriction(getLocationRestriction())
                        .setTypesFilter(getTypesFilter())
                        .setOrigin(getOrigin())
                        .build(this);
        autocompleteLauncher.launch(autocompleteIntent);
    }

    private void startBasicAutocompleteActivity() {
        Intent basicAutocompleteIntent =
                new BasicPlaceAutocomplete.IntentBuilder()
                        .setInitialQuery(getQuery())
                        .setCountries(getCountries())
                        .setLocationBias(getLocationBias())
                        .setLocationRestriction(getLocationRestriction())
                        .setTypesFilter(getTypesFilter())
                        .setOrigin(getOrigin())
                        .build(this);
        basicAutocompleteLauncher.launch(basicAutocompleteIntent);
    }

    private void findAutocompletePredictions() {
        setLoading(true);

        FindAutocompletePredictionsRequest.Builder requestBuilder =
                FindAutocompletePredictionsRequest.builder()
                        .setQuery(getQuery())
                        .setCountries(getCountries())
                        .setOrigin((getOrigin()))
                        .setLocationBias(getLocationBias())
                        .setLocationRestriction(getLocationRestriction())
                        .setTypesFilter(getTypesFilter());

        if (isUseSessionTokenChecked()) {
            requestBuilder.setSessionToken(AutocompleteSessionToken.newInstance());
        }

        Task<FindAutocompletePredictionsResponse> task =
                placesClient.findAutocompletePredictions(requestBuilder.build());

        task.addOnSuccessListener(
                (response) ->
                        binding.response.setText(
                                StringUtil.stringify(response, isDisplayRawResultsChecked())));

        task.addOnFailureListener(
                (exception) -> {
                    exception.printStackTrace();
                    binding.response.setText(exception.getMessage());
                });

        task.addOnCompleteListener(response -> setLoading(false));
    }

    //////////////////////////
    // Helper methods below //
    //////////////////////////

    private List<Place.Field> getPlaceFields() {
        if (((CheckBox) findViewById(R.id.use_custom_fields)).isChecked()) {
            return fieldSelector.getSelectedFields();
        } else {
            return fieldSelector.getAllFields();
        }
    }

    @Nullable
    private String getQuery() {
        return getTextViewValue(R.id.autocomplete_query);
    }

    @Nullable
    private String getHint() {
        return getTextViewValue(R.id.autocomplete_hint);
    }

    private List<String> getCountries() {
        String countryString = getTextViewValue(R.id.autocomplete_country);
        if (TextUtils.isEmpty(countryString)) {
            return new ArrayList<>();
        }

        return StringUtil.countriesStringToArrayList(countryString);
    }

    @Nullable
    private String getTextViewValue(@IdRes int textViewResId) {
        String value = ((TextView) findViewById(textViewResId)).getText().toString();
        return TextUtils.isEmpty(value) ? null : value;
    }

    @Nullable
    private LocationBias getLocationBias() {
        return getBounds(
                R.id.autocomplete_location_bias_south_west,
                R.id.autocomplete_location_bias_north_east);
    }

    @Nullable
    private LocationRestriction getLocationRestriction() {
        return getBounds(
                R.id.autocomplete_location_restriction_south_west,
                R.id.autocomplete_location_restriction_north_east);
    }

    @Nullable
    private RectangularBounds getBounds(int resIdSouthWest, int resIdNorthEast) {
        String southWest = ((TextView) findViewById(resIdSouthWest)).getText().toString();
        String northEast = ((TextView) findViewById(resIdNorthEast)).getText().toString();
        if (TextUtils.isEmpty(southWest) && TextUtils.isEmpty(northEast)) {
            return null;
        }

        LatLngBounds bounds = StringUtil.convertToLatLngBounds(southWest, northEast);
        if (bounds == null) {
            showErrorAlert(R.string.error_alert_message_invalid_bounds);
            return null;
        }

        return RectangularBounds.newInstance(bounds);
    }

    @Nullable
    private LatLng getOrigin() {
        String originStr =
                ((TextView) findViewById(R.id.autocomplete_location_origin)).getText().toString();
        if (TextUtils.isEmpty(originStr)) {
            return null;
        }

        LatLng origin = StringUtil.convertToLatLng(originStr);
        if (origin == null) {
            showErrorAlert(R.string.error_alert_message_invalid_origin);
            return null;
        }

        return origin;
    }

    private List<String> getTypesFilter() {
        EditText typesFilterEditText = findViewById(R.id.autocomplete_types_filter_edittext);
        return typesFilterEditText.isEnabled()
                ? Arrays.asList(typesFilterEditText.getText().toString().split("[\\s,]+"))
                : new ArrayList<>();
    }

    private boolean isDisplayRawResultsChecked() {
        return ((CheckBox) findViewById(R.id.display_raw_results)).isChecked();
    }

    private boolean isUseSessionTokenChecked() {
        return ((CheckBox) findViewById(R.id.autocomplete_use_session_token)).isChecked();
    }

    private void setLoading(boolean loading) {
        findViewById(R.id.loading).setVisibility(loading ? View.VISIBLE : View.INVISIBLE);
    }

    private void showErrorAlert(@StringRes int messageResId) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.error_alert_title)
                .setMessage(messageResId)
                .show();
    }
}

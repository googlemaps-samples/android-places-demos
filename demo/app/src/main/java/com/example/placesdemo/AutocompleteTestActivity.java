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

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.LocationBias;
import com.google.android.libraries.places.api.model.LocationRestriction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteFragment;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity for testing Autocomplete (activity and fragment widgets, and programmatic).
 */
public class AutocompleteTestActivity extends AppCompatActivity {

  private static final int AUTOCOMPLETE_REQUEST_CODE = 23487;
  private PlacesClient placesClient;
  private TextView responseView;
  private FieldSelector fieldSelector;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.autocomplete_test_activity);

    // Retrieve a PlacesClient (previously initialized - see MainActivity)
    placesClient = Places.createClient(this);

    // Set up view objects
    responseView = findViewById(R.id.response);
    Spinner typeFilterSpinner = findViewById(R.id.autocomplete_type_filter);
    typeFilterSpinner.setAdapter(
        new ArrayAdapter<>(
            this, android.R.layout.simple_list_item_1, Arrays.asList(TypeFilter.values())));
    CheckBox useTypeFilterCheckBox = findViewById(R.id.autocomplete_use_type_filter);
    useTypeFilterCheckBox.setOnCheckedChangeListener(
        (buttonView, isChecked) -> typeFilterSpinner.setEnabled(isChecked));
    fieldSelector =
        new FieldSelector(
            findViewById(R.id.use_custom_fields), findViewById(R.id.custom_fields_list));

    setupAutocompleteSupportFragment();
    setupAutocompleteFragment();

    // Set listeners for Autocomplete activity
    findViewById(R.id.autocomplete_activity_button)
            .setOnClickListener(view -> startAutocompleteActivity());

    // Set listeners for programmatic Autocomplete
    findViewById(R.id.fetch_autocomplete_predictions_button)
            .setOnClickListener(view -> findAutocompletePredictions());

    // UI initialization
    setLoading(false);
    typeFilterSpinner.setEnabled(false);
  }

  private void setupAutocompleteSupportFragment() {
    final AutocompleteSupportFragment autocompleteSupportFragment =
        (AutocompleteSupportFragment)
            getSupportFragmentManager().findFragmentById(R.id.autocomplete_support_fragment);
    autocompleteSupportFragment.setPlaceFields(getPlaceFields());
    autocompleteSupportFragment.setOnPlaceSelectedListener(getPlaceSelectionListener());
    findViewById(R.id.autocomplete_support_fragment_update_button)
        .setOnClickListener(
            view -> {
              autocompleteSupportFragment.setPlaceFields(getPlaceFields());
              autocompleteSupportFragment.setText(getQuery());
              autocompleteSupportFragment.setHint(getHint());
              autocompleteSupportFragment.setCountry(getCountry());
              autocompleteSupportFragment.setLocationBias(getLocationBias());
              autocompleteSupportFragment.setLocationRestriction(getLocationRestriction());
              autocompleteSupportFragment.setTypeFilter(getTypeFilter());
            });

  }

  private void setupAutocompleteFragment() {
    final AutocompleteFragment autocompleteFragment =
            (AutocompleteFragment) getFragmentManager().findFragmentById(R.id.autocomplete_fragment);
    autocompleteFragment.setPlaceFields(getPlaceFields());
    autocompleteFragment.setOnPlaceSelectedListener(getPlaceSelectionListener());
    findViewById(R.id.autocomplete_fragment_update_button)
            .setOnClickListener(
                    view -> {
                      autocompleteFragment.setPlaceFields(getPlaceFields());
                      autocompleteFragment.setText(getQuery());
                      autocompleteFragment.setHint(getHint());
                      autocompleteFragment.setCountry(getCountry());
                      autocompleteFragment.setLocationBias(getLocationBias());
                      autocompleteFragment.setLocationRestriction(getLocationRestriction());
                      autocompleteFragment.setTypeFilter(getTypeFilter());
                    });
  }

  @NonNull
  private PlaceSelectionListener getPlaceSelectionListener() {
    return new PlaceSelectionListener() {
      @Override
      public void onPlaceSelected(Place place) {
        responseView.setText(
                StringUtil.stringifyAutocompleteWidget(place, isDisplayRawResultsChecked()));
      }

      @Override
      public void onError(Status status) {
        responseView.setText(status.getStatusMessage());
      }
    };
  }

  /**
   * Called when AutocompleteActivity finishes
   */
  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
    if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
      if (resultCode == AutocompleteActivity.RESULT_OK) {
        Place place = Autocomplete.getPlaceFromIntent(intent);
        responseView.setText(
            StringUtil.stringifyAutocompleteWidget(place, isDisplayRawResultsChecked()));
      } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
        Status status = Autocomplete.getStatusFromIntent(intent);
        responseView.setText(status.getStatusMessage());
      } else if (resultCode == AutocompleteActivity.RESULT_CANCELED) {
        // The user canceled the operation.
      }
    }

    // Required because this class extends AppCompatActivity which extends FragmentActivity
    // which implements this method to pass onActivityResult calls to child fragments
    // (eg AutocompleteFragment).
    super.onActivityResult(requestCode, resultCode, intent);
  }

  private void startAutocompleteActivity() {
    Intent autocompleteIntent =
        new Autocomplete.IntentBuilder(getMode(), getPlaceFields())
            .setInitialQuery(getQuery())
            .setCountry(getCountry())
            .setLocationBias(getLocationBias())
            .setLocationRestriction(getLocationRestriction())
            .setTypeFilter(getTypeFilter())
            .build(AutocompleteTestActivity.this);
    startActivityForResult(autocompleteIntent, AUTOCOMPLETE_REQUEST_CODE);
  }

  private void findAutocompletePredictions() {
    setLoading(true);

    FindAutocompletePredictionsRequest.Builder requestBuilder =
        FindAutocompletePredictionsRequest.builder()
            .setQuery(getQuery())
            .setCountry(getCountry())
            .setLocationBias(getLocationBias())
            .setLocationRestriction(getLocationRestriction())
            .setTypeFilter(getTypeFilter());

    if (isUseSessionTokenChecked()) {
      requestBuilder.setSessionToken(AutocompleteSessionToken.newInstance());
    }

    Task<FindAutocompletePredictionsResponse> task =
        placesClient.findAutocompletePredictions(requestBuilder.build());

    task.addOnSuccessListener(
        (response) ->
            responseView.setText(StringUtil.stringify(response, isDisplayRawResultsChecked())));

    task.addOnFailureListener(
        (exception) -> {
          exception.printStackTrace();
          responseView.setText(exception.getMessage());
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

  private String getQuery() {
    return ((TextView) findViewById(R.id.autocomplete_query)).getText().toString();
  }

  private String getHint() {
    return ((TextView) findViewById(R.id.autocomplete_hint)).getText().toString();
  }

  private String getCountry() {
    return ((TextView) findViewById(R.id.autocomplete_country)).getText().toString();
  }

  @Nullable
  private LocationBias getLocationBias() {
    return getBounds(
        R.id.autocomplete_location_bias_south_west, R.id.autocomplete_location_bias_north_east);
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
  private TypeFilter getTypeFilter() {
    Spinner typeFilter = findViewById(R.id.autocomplete_type_filter);
    return typeFilter.isEnabled() ? (TypeFilter) typeFilter.getSelectedItem() : null;
  }

  private AutocompleteActivityMode getMode() {
    boolean isOverlayMode =
        ((CheckBox) findViewById(R.id.autocomplete_activity_overlay_mode)).isChecked();
    return isOverlayMode ? AutocompleteActivityMode.OVERLAY : AutocompleteActivityMode.FULLSCREEN;
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

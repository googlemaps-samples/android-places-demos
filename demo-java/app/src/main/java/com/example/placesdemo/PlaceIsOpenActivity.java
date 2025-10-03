/*
 * Copyright 2025 Google LLC
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

package com.example.placesdemo;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.placesdemo.databinding.PlaceIsOpenActivityBinding;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.Place.Field;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.IsOpenRequest;
import com.google.android.libraries.places.api.net.IsOpenResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import androidx.activity.EdgeToEdge;

/**
 * Activity to demonstrate {@link PlacesClient#isOpen(IsOpenRequest)}.
 */
public class PlaceIsOpenActivity extends AppCompatActivity {
    private final String defaultTimeZone = "America/Los_Angeles";
    @NonNull
    private Calendar isOpenCalendar = Calendar.getInstance();
    private PlaceIsOpenActivityBinding binding;

    private FieldSelector fieldSelector;
    private PlacesClient placesClient;
    private Place place;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Enable edge-to-edge display. This must be called before calling super.onCreate().
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);

        binding = PlaceIsOpenActivityBinding.inflate(getLayoutInflater());
        View rootView = binding.getRoot();
        setContentView(rootView);

        // Retrieve a PlacesClient (previously initialized - see MainActivity)
        placesClient = Places.createClient(/* context= */ this);

        fieldSelector =
                new FieldSelector(
                        binding.checkBoxUseCustomFields,
                        binding.textViewCustomFieldsList,
                        savedInstanceState);

        binding.buttonFetchPlace.setOnClickListener(view -> fetchPlace());
        binding.buttonIsOpen.setOnClickListener(view -> isOpenByPlaceId());

        isOpenCalendar = Calendar.getInstance(TimeZone.getTimeZone(defaultTimeZone));

        // UI initialization
        setLoading(false);
        initializeSpinnerAndAddListener();
        addIsOpenDateSelectionListener();
        addIsOpenTimeSelectionListener();

        updateIsOpenDate();
        updateIsOpenTime();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        fieldSelector.onSaveInstanceState(bundle);
    }

    /**
     * Get details about the Place ID listed in the input field, then check if the Place is open.
     */
    private void fetchPlace() {
        clearViews();
        dismissKeyboard(binding.editTextPlaceId);
        setLoading(true);

        List<Field> placeFields = getPlaceFields();
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(getPlaceId(), placeFields);
        Task<FetchPlaceResponse> placeTask = placesClient.fetchPlace(request);

        placeTask.addOnSuccessListener(
                (response) -> {
                    place = response.getPlace();
                    isOpenByPlaceObject(place);
                });

        placeTask.addOnFailureListener(
                (exception) -> {
                    exception.printStackTrace();
                    binding.textViewResponse.setText(exception.getMessage());
                });

        placeTask.addOnCompleteListener(response -> setLoading(false));
    }

    /**
     * Check if the place is open at the time specified in the input fields.
     * Requires a Place object that includes Place.Field.ID
     */
    @SuppressLint("SetTextI18n")
    private void isOpenByPlaceObject(Place place) {
        clearViews();
        dismissKeyboard(binding.editTextPlaceId);
        setLoading(true);

        IsOpenRequest request;

        try {
            request = IsOpenRequest.newInstance(place, isOpenCalendar.getTimeInMillis());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            binding.textViewResponse.setText(e.getMessage());
            setLoading(false);
            return;
        }

        Task<IsOpenResponse> placeTask = placesClient.isOpen(request);

        placeTask.addOnSuccessListener(
                (response) -> binding.textViewResponse.setText("Is place open? "
                                                 + response.isOpen()
                                                 + "\nExtra place details: \n"
                                                 + StringUtil.stringify(place)));

        placeTask.addOnFailureListener(
                (exception) -> {
                    exception.printStackTrace();
                    binding.textViewResponse.setText(exception.getMessage());
                });

        placeTask.addOnCompleteListener(response -> setLoading(false));
    }

    /**
     * Check if the place is open at the time specified in the input fields.
     * Use the Place ID in the input field for the isOpenRequest.
     */
    @SuppressLint("SetTextI18n")
    private void isOpenByPlaceId() {
        clearViews();
        dismissKeyboard(binding.editTextPlaceId);
        setLoading(true);

        IsOpenRequest request;

        try {
            request = IsOpenRequest.newInstance(getPlaceId(), isOpenCalendar.getTimeInMillis());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            binding.textViewResponse.setText(e.getMessage());
            setLoading(false);
            return;
        }

        Task<IsOpenResponse> placeTask = placesClient.isOpen(request);

        placeTask.addOnSuccessListener(
                (response) -> binding.textViewResponse.setText("Is place open? " + response.isOpen()));

        placeTask.addOnFailureListener(
                (exception) -> {
                    exception.printStackTrace();
                    binding.textViewResponse.setText(exception.getMessage());
                });

        placeTask.addOnCompleteListener(response -> setLoading(false));
    }

    //////////////////////////
    // Helper methods below //
    //////////////////////////

    private void dismissKeyboard(EditText focusedEditText) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(focusedEditText.getWindowToken(), 0);
    }

    private String getPlaceId() {
        return ((TextView) binding.editTextPlaceId).getText().toString();
    }

    /**
     * Fetch the fields necessary for an isOpen request, unless user has checked the box to
     * select a custom list of fields. Also fetches name and address for display text.
     */
    private List<Field> getPlaceFields() {
        if (((CheckBox) binding.checkBoxUseCustomFields).isChecked()) {
            return fieldSelector.getSelectedFields();
        } else {
            return new ArrayList<>(Arrays.asList(
                    Field.FORMATTED_ADDRESS,
                    Field.BUSINESS_STATUS,
                    Field.CURRENT_OPENING_HOURS,
                    Field.ID,
                    Field.DISPLAY_NAME,
                    Field.OPENING_HOURS,
                    Field.UTC_OFFSET
            ));
        }
    }

    private void setLoading(boolean loading) {
        binding.progressBarLoading.setVisibility(loading ? View.VISIBLE : View.INVISIBLE);
    }

    private void clearViews() {
        binding.textViewResponse.setText(null);
    }

    private void initializeSpinnerAndAddListener() {
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, TimeZone.getAvailableIDs());
        binding.spinnerTimeZones.setAdapter(adapter);
        binding.spinnerTimeZones.setSelection(adapter.getPosition(defaultTimeZone));

        binding.spinnerTimeZones.setOnItemSelectedListener(
                new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String timeZone = parent.getItemAtPosition(position).toString();
                        isOpenCalendar.setTimeZone(TimeZone.getTimeZone(timeZone));
                        updateIsOpenDate();
                        updateIsOpenTime();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
    }

    private void addIsOpenDateSelectionListener() {
        DatePickerDialog.OnDateSetListener listener =
                (view, year, month, day) -> {
                    isOpenCalendar.set(Calendar.YEAR, year);
                    isOpenCalendar.set(Calendar.MONTH, month);
                    isOpenCalendar.set(Calendar.DAY_OF_MONTH, day);
                    updateIsOpenDate();
                };

        binding.editTextIsOpenDate.setOnClickListener(
                view -> new DatePickerDialog(
                        PlaceIsOpenActivity.this,
                        listener,
                        isOpenCalendar.get(Calendar.YEAR),
                        isOpenCalendar.get(Calendar.MONTH),
                        isOpenCalendar.get(Calendar.DAY_OF_MONTH))
                        .show());
    }

    private void updateIsOpenDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy", Locale.US);
        binding.editTextIsOpenDate.setText(dateFormat.format(isOpenCalendar.getTime()));
    }

    private void addIsOpenTimeSelectionListener() {
        TimePickerDialog.OnTimeSetListener listener =
                (view, hourOfDay, minute) -> {
                    isOpenCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    isOpenCalendar.set(Calendar.MINUTE, minute);
                    updateIsOpenTime();
                };

        binding.editTextIsOpenTime.setOnClickListener(
                view -> new TimePickerDialog(
                        PlaceIsOpenActivity.this,
                        listener,
                        isOpenCalendar.get(Calendar.HOUR_OF_DAY),
                        isOpenCalendar.get(Calendar.MINUTE),
                        true)
                        .show());
    }

    private void updateIsOpenTime() {
        String formattedHour =
                String.format(Locale.getDefault(), "%02d", isOpenCalendar.get(Calendar.HOUR_OF_DAY));
        String formattedMinutes =
                String.format(Locale.getDefault(), "%02d", isOpenCalendar.get(Calendar.MINUTE));
        binding.editTextIsOpenTime.setText(
                String.format(Locale.getDefault(), "%s:%s", formattedHour, formattedMinutes));
    }
}
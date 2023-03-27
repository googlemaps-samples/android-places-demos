package com.example.placesdemo;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.Nullable;
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

/**
 * Activity to demonstrate {@link PlacesClient#isOpen(IsOpenRequest)}.
 */
public class PlaceIsOpenActivity extends AppCompatActivity {
    private final String defaultTimeZone = "America/Los_Angeles";
    @NonNull
    private final Calendar isOpenCalendar = Calendar.getInstance();

    private EditText editTextIsOpenDate;
    private EditText editTextIsOpenTime;
    private TextView textViewResponse;
    private Spinner spinnerTimeZones;
    private FieldSelector fieldSelector;
    private PlacesClient placesClient;
    private Place place;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.place_is_open_activity);

        // Retrieve a PlacesClient (previously initialized - see MainActivity)
        placesClient = Places.createClient(/* context= */ this);

        textViewResponse = findViewById(R.id.textView_response);
        spinnerTimeZones = findViewById(R.id.spinner_timeZones);
        editTextIsOpenDate = findViewById(R.id.editText_isOpenDate);
        editTextIsOpenTime = findViewById(R.id.editText_isOpenTime);

        fieldSelector =
                new FieldSelector(
                        findViewById(R.id.checkBox_useCustomFields),
                        findViewById(R.id.textView_customFieldsList),
                        savedInstanceState);

        findViewById(R.id.button_fetchPlace).setOnClickListener(view -> fetchPlace());
        findViewById(R.id.button_isOpen).setOnClickListener(view -> isOpen());

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
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        fieldSelector.onSaveInstanceState(bundle);
    }

    private void fetchPlace() {
        clearViews();
        dismissKeyboard(findViewById(R.id.editText_placeId));
        setLoading(true);

        List<Field> placeFields = getPlaceFields();
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(getPlaceId(), placeFields);
        Task<FetchPlaceResponse> placeTask = placesClient.fetchPlace(request);

        placeTask.addOnSuccessListener(
                (response) -> {
                    place = response.getPlace();
                    textViewResponse.setText(StringUtil.stringify(response, isDisplayRawResultsChecked()));
                });

        placeTask.addOnFailureListener(
                (exception) -> {
                    exception.printStackTrace();
                    textViewResponse.setText(exception.getMessage());
                });

        placeTask.addOnCompleteListener(response -> setLoading(false));
    }

    @SuppressLint("SetTextI18n")
    private void isOpen() {
        clearViews();
        dismissKeyboard(findViewById(R.id.editText_placeId));
        setLoading(true);

        IsOpenRequest request;

        try {
            request =
                    place != null
                            ? IsOpenRequest.newInstance(place, isOpenCalendar.getTimeInMillis())
                            : IsOpenRequest.newInstance(getPlaceId(), isOpenCalendar.getTimeInMillis());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            textViewResponse.setText(e.getMessage());
            setLoading(false);
            return;
        }

        Task<IsOpenResponse> placeTask = placesClient.isOpen(request);

        placeTask.addOnSuccessListener(
                (response) -> {
                    textViewResponse.setText("Is place open? " + response.isOpen());
                });

        placeTask.addOnFailureListener(
                (exception) -> {
                    exception.printStackTrace();
                    textViewResponse.setText(exception.getMessage());
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
        return ((TextView) findViewById(R.id.editText_placeId)).getText().toString();
    }

    private List<Field> getPlaceFields() {
        if (((CheckBox) findViewById(R.id.checkBox_useCustomFields)).isChecked()) {
            return fieldSelector.getSelectedFields();
        } else {
            return new ArrayList<Field>(Arrays.asList(
                    Field.BUSINESS_STATUS,
                    Field.CURRENT_OPENING_HOURS,
                    Field.OPENING_HOURS,
                    Field.UTC_OFFSET
            );
        }
    }

    private boolean isDisplayRawResultsChecked() {
        return ((CheckBox) findViewById(R.id.checkBox_displayRawResults)).isChecked();
    }

    private void setLoading(boolean loading) {
        findViewById(R.id.progressBar_loading).setVisibility(loading ? View.VISIBLE : View.INVISIBLE);
    }

    private void clearViews() {
        textViewResponse.setText(null);
    }

    private void initializeSpinnerAndAddListener() {
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, TimeZone.getAvailableIDs());
        spinnerTimeZones.setAdapter(adapter);
        spinnerTimeZones.setSelection(adapter.getPosition(defaultTimeZone));

        spinnerTimeZones.setOnItemSelectedListener(
                new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String timeZone = parent.getItemAtPosition(position).toString();
                        isOpenCalendar.setTimeZone(TimeZone.getTimeZone(timeZone));
                        updateIsOpenDate();
                        updateIsOpenTime();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
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

        editTextIsOpenDate.setOnClickListener(
                view -> {
                    new DatePickerDialog(
                            PlaceIsOpenActivity.this,
                            listener,
                            isOpenCalendar.get(Calendar.YEAR),
                            isOpenCalendar.get(Calendar.MONTH),
                            isOpenCalendar.get(Calendar.DAY_OF_MONTH))
                            .show();
                });
    }

    private void updateIsOpenDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy", Locale.US);
        editTextIsOpenDate.setText(dateFormat.format(isOpenCalendar.getTime()));
    }

    private void addIsOpenTimeSelectionListener() {
        TimePickerDialog.OnTimeSetListener listener =
                (view, hourOfDay, minute) -> {
                    isOpenCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    isOpenCalendar.set(Calendar.MINUTE, minute);
                    updateIsOpenTime();
                };

        editTextIsOpenTime.setOnClickListener(
                view -> {
                    new TimePickerDialog(
                            PlaceIsOpenActivity.this,
                            listener,
                            isOpenCalendar.get(Calendar.HOUR_OF_DAY),
                            isOpenCalendar.get(Calendar.MINUTE),
                            true)
                            .show();
                });
    }

    private void updateIsOpenTime() {
        String formattedHour =
                String.format(Locale.getDefault(), "%02d", isOpenCalendar.get(Calendar.HOUR_OF_DAY));
        String formattedMinutes =
                String.format(Locale.getDefault(), "%02d", isOpenCalendar.get(Calendar.MINUTE));
        editTextIsOpenTime.setText(
                String.format(Locale.getDefault(), "%s:%s", formattedHour, formattedMinutes));
    }
}
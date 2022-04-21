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

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place.Field;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_WIFI_STATE;

/**
 * Activity for testing {@link PlacesClient#findCurrentPlace(FindCurrentPlaceRequest)}.
 */
public class CurrentPlaceTestActivity extends AppCompatActivity {

    private static final String TAG = CurrentPlaceTestActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_CODE = 9;

    private PlacesClient placesClient;
    private TextView responseView;
    private FieldSelector fieldSelector;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Use whatever theme was set from the MainActivity.
        int theme = getIntent().getIntExtra(MainActivity.THEME_RES_ID_EXTRA, 0);
        if (theme != 0) {
            setTheme(theme);
        }

        setContentView(R.layout.current_place_test_activity);

        // Retrieve a PlacesClient (previously initialized - see MainActivity)
        placesClient = Places.createClient(this);

        // Set view objects
        List<Field> placeFields =
            FieldSelector.allExcept(
                Field.ADDRESS_COMPONENTS,
                Field.OPENING_HOURS,
                Field.PHONE_NUMBER,
                Field.UTC_OFFSET,
                Field.WEBSITE_URI);
        fieldSelector =
            new FieldSelector(
                findViewById(R.id.use_custom_fields),
                findViewById(R.id.custom_fields_list),
                placeFields,
                savedInstanceState);
        responseView = findViewById(R.id.response);
        setLoading(false);

        // Set listeners for programmatic Find Current Place
        findViewById(R.id.find_current_place_button)
            .setOnClickListener((view) -> findCurrentPlace());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        fieldSelector.onSaveInstanceState(bundle);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults) {
        if (requestCode != PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (isLocationPermissionGranted(permissions, grantResults)) {
            findCurrentPlaceWithPermissions();
        }
    }

    private boolean isLocationPermissionGranted(String[] permissions, int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            final String permission = permissions[i];
            switch (permission) {
                case ACCESS_FINE_LOCATION:
                case ACCESS_COARSE_LOCATION:
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        return true;
                    }
            }
        }
        return false;
    }


    /**
     * Fetches a list of {@link PlaceLikelihood} instances that represent the Places the user is
     * most likely to be at currently.
     */
    @SuppressLint("MissingPermission")
    private void findCurrentPlace() {
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(
                this,
                "Either ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION permission is required.",
                Toast.LENGTH_SHORT)
                .show();
        }

        // 1. Check if either ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION is granted. If so,
        // proceed with finding the current place.
        if (hasOnePermissionGranted(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)) {
            Log.d(TAG, "Location permission granted. Getting current place.");
            findCurrentPlaceWithPermissions();
            return;
        }

        // 2. If either permission is not granted, check if a permission rationale dialog must be
        // shown
        if (shouldShowPermissionRationale(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)) {
            Log.d(TAG, "Showing permission rationale dialog");
            RationaleDialog.newInstance(
                PERMISSION_REQUEST_CODE, true
            ).show(getSupportFragmentManager(), "dialog");
            return;
        }

        // 3. Otherwise, request permission
        Log.d(TAG, "No location permission granted. Request permission from the user.");
        ActivityCompat
            .requestPermissions(this, new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION},
                PERMISSION_REQUEST_CODE);
    }

    /**
     * Fetches a list of {@link PlaceLikelihood} instances that represent the Places the user is
     * most likely to be at currently.
     */
    @RequiresPermission(anyOf = {ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION})
    private void findCurrentPlaceWithPermissions() {
        setLoading(true);

        FindCurrentPlaceRequest currentPlaceRequest =
            FindCurrentPlaceRequest.newInstance(getPlaceFields());

        // Safe to suppress permission for ACCESS_WIFI_STATE since this is added in the manifest
        // file by the Places SDK
        @SuppressLint("MissingPermission") Task<FindCurrentPlaceResponse> currentPlaceTask =
            placesClient.findCurrentPlace(currentPlaceRequest);

        currentPlaceTask.addOnSuccessListener(
            (response) ->
                responseView.setText(StringUtil.stringify(response, isDisplayRawResultsChecked())));

        currentPlaceTask.addOnFailureListener(
            (exception) -> {
                exception.printStackTrace();
                responseView.setText(exception.getMessage());
            });

        currentPlaceTask.addOnCompleteListener(task -> setLoading(false));
    }

    //////////////////////////
    // Helper methods below //
    //////////////////////////

    private List<Field> getPlaceFields() {
        if (((CheckBox) findViewById(R.id.use_custom_fields)).isChecked()) {
            return fieldSelector.getSelectedFields();
        } else {
            return fieldSelector.getAllFields();
        }
    }

    private boolean shouldShowPermissionRationale(String... permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasOnePermissionGranted(String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }

    private boolean isDisplayRawResultsChecked() {
        return ((CheckBox) findViewById(R.id.display_raw_results)).isChecked();
    }

    private void setLoading(boolean loading) {
        findViewById(R.id.loading).setVisibility(loading ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * A dialog that explains the use of the location permission and requests the necessary
     * permission.
     * <p>
     * The activity should implement {@link androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback}
     * to handle permit or denial of this permission request.
     */
    public static class RationaleDialog extends DialogFragment {

        private static final String ARGUMENT_PERMISSION_REQUEST_CODE = "requestCode";

        private static final String ARGUMENT_FINISH_ACTIVITY = "finish";

        private boolean finishActivity = false;

        /**
         * Creates a new instance of a dialog displaying the rationale for the use of the location
         * permission.
         * <p>
         * The permission is requested after clicking 'ok'.
         *
         * @param requestCode Id of the request that is used to request the permission. It is
         * returned to the {@link androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback}.
         * @param finishActivity Whether the calling Activity should be finished if the dialog is
         * cancelled.
         */
        public static RationaleDialog newInstance(int requestCode, boolean finishActivity) {
            Bundle arguments = new Bundle();
            arguments.putInt(ARGUMENT_PERMISSION_REQUEST_CODE, requestCode);
            arguments.putBoolean(ARGUMENT_FINISH_ACTIVITY, finishActivity);
            RationaleDialog dialog = new RationaleDialog();
            dialog.setArguments(arguments);
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle arguments = getArguments();
            final int requestCode = arguments.getInt(ARGUMENT_PERMISSION_REQUEST_CODE);
            finishActivity = arguments.getBoolean(ARGUMENT_FINISH_ACTIVITY);

            return new AlertDialog.Builder(requireActivity())
                .setMessage(R.string.permission_rationale_location)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    // After click on Ok, request the permission.
                    ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION},
                        requestCode);
                    // Do not finish the Activity while requesting permission.
                    finishActivity = false;
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        }

        @Override
        public void onDismiss(@NonNull DialogInterface dialog) {
            super.onDismiss(dialog);
            if (finishActivity) {
                Toast.makeText(requireContext(),
                    R.string.permission_required_toast,
                    Toast.LENGTH_SHORT)
                    .show();
                requireActivity().finish();
            }
        }
    }
}

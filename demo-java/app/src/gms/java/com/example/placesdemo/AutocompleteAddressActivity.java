package com.example.placesdemo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.placesdemo.model.AutocompleteEditText;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.AddressComponents;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.util.Log.e;
import static android.util.Log.i;
import static com.google.maps.android.SphericalUtil.computeDistanceBetween;

/**
 * Activity for using Place Autocomplete to assist filling out an address form.
 */
@SuppressWarnings("FieldCanBeLocal")
public class AutocompleteAddressActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "ADDRESS_AUTOCOMPLETE";
    private static final String MAP_FRAGMENT_TAG = "MAP";
    private static final int AUTOCOMPLETE_REQUEST_CODE = 23487;
    private AutocompleteEditText address1Field;
    private EditText address2Field;
    private EditText cityField;
    private EditText stateField;
    private EditText postalField;
    private EditText countryField;
    private LatLng coordinates;
    private Boolean checkProximity = false;
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private Marker marker;
    private PlacesClient placesClient;
    private View mapPanel;
    private LatLng deviceLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Use whatever theme was set from the MainActivity - some of these colors (e.g primary color)
        // will get picked up by the AutocompleteActivity.
        int theme = getIntent().getIntExtra(MainActivity.THEME_RES_ID_EXTRA, 0);
        if (theme != 0) {
            setTheme(theme);
        }

        setContentView(R.layout.autocomplete_address_activity);

        // Retrieve a PlacesClient (previously initialized - see MainActivity)
        placesClient = Places.createClient(this);

        address1Field = findViewById(R.id.autocomplete_address1);
        address2Field = findViewById(R.id.autocomplete_address2);
        cityField = findViewById(R.id.autocomplete_city);
        stateField = findViewById(R.id.autocomplete_state);
        postalField = findViewById(R.id.autocomplete_postal);
        countryField = findViewById(R.id.autocomplete_country);

        // Attach an Autocomplete intent to the Address 1 EditText field
        address1Field.setOnClickListener(v -> startAutocompleteIntent());

        // Submit and optionally check proximity
        Button saveButton = findViewById(R.id.autocomplete_save_button);
        saveButton.setOnClickListener(v -> saveForm());

        // Reset the form
        Button resetButton = findViewById(R.id.autocomplete_reset_button);
        resetButton.setOnClickListener(v -> clearForm());
    }

    private void startAutocompleteIntent() {
        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        List<Place.Field> fields = Arrays.asList(Place.Field.ADDRESS_COMPONENTS,
                Place.Field.LAT_LNG, Place.Field.VIEWPORT);

        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                i(TAG, "Place: " + place.getAddressComponents());

                fillInAddress(place);

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a string resource.
            boolean success = map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));

            if (!success) {
                e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            e(TAG, "Can't find style. Error: ", e);
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 15f));
        marker = map.addMarker(new MarkerOptions().position(coordinates));
    }

    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch (view.getId()) {
            case R.id.checkbox_proximity:
                checkProximity = checked;
                break;
        }
    }

    private void fillInAddress(Place place) {
        AddressComponents components = place.getAddressComponents();
        StringBuilder address1 = new StringBuilder();
        StringBuilder postcode = new StringBuilder();

        // Get each component of the address from the place details,
        // and then fill-in the corresponding field on the form.
        // Possible AddressComponent types are documented at https://goo.gle/32SJPM1
        if (components != null) {
            for (AddressComponent component : components.asList()) {
                String type = component.getTypes().get(0);
                switch (type) {
                    case "street_number": {
                        address1.insert(0, component.getName());
                        break;
                    }

                    case "route": {
                        address1.append(" ");
                        address1.append(component.getShortName());
                        break;
                    }

                    case "postal_code": {
                        postcode.insert(0, component.getName());
                        break;
                    }

                    case "postal_code_suffix": {
                        postcode.append("-").append(component.getName());
                        break;
                    }

                    case "locality":
                        cityField.setText(component.getName());
                        break;

                    case "administrative_area_level_1": {
                        stateField.setText(component.getShortName());
                        break;
                    }

                    case "country":
                        countryField.setText(component.getName());
                        break;
                }
            }
        }

        address1Field.setText(address1.toString());
        postalField.setText(postcode.toString());

        // After filling the form with address components from the Autocomplete
        // prediction, set cursor focus on the second address line to encourage
        // entry of sub-premise information such as apartment, unit, or floor number.
        address2Field.requestFocus();

        // Add a map for visual confirmation of the address
        showMap(place);
    }

    private void showMap(Place place) {
        coordinates = place.getLatLng();

        // It isn't possible to set a fragment's id programmatically so we set a tag instead and
        // search for it using that.
        mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentByTag(MAP_FRAGMENT_TAG);

        // We only create a fragment if it doesn't already exist.
        if (mapFragment == null) {
            mapPanel = ((ViewStub) findViewById(R.id.stub_map)).inflate();
            GoogleMapOptions mapOptions = new GoogleMapOptions();
            mapOptions.mapToolbarEnabled(false);

            // To programmatically add the map, we first create a SupportMapFragment.
            mapFragment = SupportMapFragment.newInstance(mapOptions);

            // Then we add it using a FragmentTransaction.
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.confirmation_map, mapFragment, MAP_FRAGMENT_TAG)
                    .commit();
            mapFragment.getMapAsync(this);
        } else {
            updateMap(coordinates);
        }
    }

    private void updateMap(LatLng latLng) {
        marker.setPosition(latLng);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
        if (mapPanel.getVisibility() == View.GONE) {
            mapPanel.setVisibility(View.VISIBLE);
        }
    }

    private void saveForm() {
        if (checkProximity) {
            checkLocationPermissions();
        } else {
            Toast.makeText(
                    this,
                    "Address accepted without checking proximity",
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void clearForm() {
        address1Field.setText("");
        address2Field.getText().clear();
        cityField.getText().clear();
        stateField.getText().clear();
        postalField.getText().clear();
        countryField.getText().clear();
        if (mapPanel != null) {
            mapPanel.setVisibility(View.GONE);
        }
        address1Field.requestFocus();
    }

    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher, as an instance variable.
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), this::onActivityResult);

    private void checkLocationPermissions() {
        Toast toast = Toast.makeText(getApplicationContext(), "checking proximity", Toast.LENGTH_SHORT);
        toast.show();
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "fine location permission granted");
            getAndCompareLocations();
        } else {
            requestPermissionLauncher.launch(
                    ACCESS_FINE_LOCATION);
        }
    }

    private void onActivityResult(Boolean isGranted) {
        if (isGranted) {
            Log.d(TAG, "permission granted upon request");
            getAndCompareLocations();
        } else {
            Log.d(TAG, "permission denied");
            Toast.makeText(
                    this,
                    "User denied location permission. Cannot check proximity to entered address.",
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @SuppressLint("MissingPermission")
    private void getAndCompareLocations() {
        // TODO: Detect and handle if user has entered or modified the address manually and update
        // the coordinates variable to the Lat/Lng of the manually entered address. May use
        // Geocoding API to convert the manually entered address to a Lat/Lng.
        LatLng enteredLocation = coordinates;

        FusedLocationProviderClient fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        map.setMyLocationEnabled(true);
                        deviceLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        Log.d(TAG, deviceLocation.toString());
                        Log.d(TAG, enteredLocation.toString());

                        // Use the computeDistanceBetween function in the Maps SDK for Android Utility Library
                        // to use spherical geometry to compute the distance between two Lat/Lng points.
                        double distanceInMeters = computeDistanceBetween(deviceLocation, enteredLocation);
                        if (distanceInMeters < 150) {
                            Log.d(TAG, "location matched");
                            Toast.makeText(
                                    getApplicationContext(),
                                    "Device is within 150 meters of the entered address.",
                                    Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            Log.d(TAG, "location not matched");
                            Toast.makeText(
                                    getApplicationContext(),
                                    "Device is more than 150 meters from the entered address.",
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
    }
}
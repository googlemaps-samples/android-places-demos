package com.example.placesdemo;

import android.annotation.SuppressLint;
import android.app.Activity;
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

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.placesdemo.model.AutocompleteEditText;
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
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
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
    private boolean checkProximity = false;
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private Marker marker;
    private PlacesClient placesClient;
    private View mapPanel;
    private LatLng deviceLocation;
    private static final double acceptableProximity = 150;

    private ActivityResultLauncher<Intent> startAutocomplete = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            (ActivityResultCallback<ActivityResult>) result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = result.getData();
                    Place place = Autocomplete.getPlaceFromIntent(intent);
                    Log.d(TAG, "Place: " + place.getAddressComponents());

                    fillInAddress(place);
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    // The user canceled the operation.
                }
            });

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

        // Update checkProximity when user checks the checkbox
        CheckBox checkProximityBox = findViewById(R.id.checkbox_proximity);
        checkProximityBox.setOnCheckedChangeListener((view, isChecked) -> {
            // Set the boolean to match user preference for when the Submit button is clicked
            checkProximity = isChecked;
        });

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
        startAutocomplete.launch(intent);
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
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 15f));
        marker = map.addMarker(new MarkerOptions().position(coordinates));
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
        Log.d(TAG,"checkProximity = " + String.valueOf(checkProximity));
        if (checkProximity) {
            checkLocationPermissions();
        } else {
            Toast.makeText(
                    this,
                    R.string.autocomplete_skipped_message,
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
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d(TAG, "fine location permission granted upon request");
                    getAndCompareLocations();
                } else {
                    Log.d(TAG, "fine location permission denied");
                    Toast.makeText(
                            this,
                            R.string.autocomplete_denied_message,
                            Toast.LENGTH_SHORT)
                            .show();
                }
            });

    private void checkLocationPermissions() {
        Toast.makeText(
                getApplicationContext(),
                R.string.autocomplete_progress_message,
                Toast.LENGTH_SHORT)
                .show();
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "fine location permission already granted");
            getAndCompareLocations();
        } else {
            requestPermissionLauncher.launch(
                    ACCESS_FINE_LOCATION);
        }
    }

    @SuppressLint("MissingPermission")
    private void getAndCompareLocations() {
        // TODO: Detect and handle if user has entered or modified the address manually and update
        // the coordinates variable to the Lat/Lng of the manually entered address. May use
        // Geocoding API to convert the manually entered address to a Lat/Lng.
        LatLng enteredLocation = coordinates;
        map.setMyLocationEnabled(true);

        FusedLocationProviderClient fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    // Got last known location. In some rare situations this can be null.
                    if (location == null) {
                        return;
                    }

                    deviceLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    Log.d(TAG, "device location = " + deviceLocation.toString());
                    Log.d(TAG, "entered location = " + enteredLocation.toString());

                    // Use the computeDistanceBetween function in the Maps SDK for Android Utility Library
                    // to use spherical geometry to compute the distance between two Lat/Lng points.
                    double distanceInMeters = computeDistanceBetween(deviceLocation, enteredLocation);
                    if (distanceInMeters <= acceptableProximity) {
                        Log.d(TAG, "location matched");
                        Toast.makeText(
                                getApplicationContext(),
                                R.string.autocomplete_match_message,
                                Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        Log.d(TAG, "location not matched");
                        Toast.makeText(
                                getApplicationContext(),
                                R.string.autocomplete_nomatch_message,
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }
}
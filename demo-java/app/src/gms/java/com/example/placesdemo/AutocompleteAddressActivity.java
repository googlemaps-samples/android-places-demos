package com.example.placesdemo;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.Status;
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
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;

/**
 * Activity for using Place Autocomplete to assist filling out an address form.
 */
public class AutocompleteAddressActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "ADDRESSAUTOCOMPLETE";
    private static final int AUTOCOMPLETE_REQUEST_CODE = 23487;
    private PlacesClient placesClient;
    private LatLng coordinates;
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private Marker marker;
    private final OnMapReadyCallback callbackObject = this;

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
        EditText address1Field = findViewById(R.id.autocomplete_address1);
        address1Field.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(MotionEvent.ACTION_UP == event.getAction()) {
                    startAutocompleteIntent();
                }

                return true;
            }
        });

        // Retrieve a PlacesClient (previously initialized - see MainActivity)
        placesClient = Places.createClient(this);

//        setupAutocompleteSupportFragment();
    }



//    private void setupAutocompleteSupportFragment() {
//        // Initialize the AutocompleteSupportFragment.
//        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
//                getSupportFragmentManager().findFragmentById(R.id.autocomplete_support_fragment_address);
//
//        // Specify the types of place data to return.
//        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ADDRESS_COMPONENTS,
//                Place.Field.LAT_LNG, Place.Field.VIEWPORT));
//
//        // Set up a PlaceSelectionListener to handle the response.
//        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
//            @Override
//            public void onPlaceSelected(@NonNull Place place) {
//                // TODO: Get info about the selected place.
//                Log.i(TAG, "Place: " + place.getAddressComponents());
//
//                coordinates = place.getLatLng();
//                if (mapFragment == null) {
//                    View mapPanel = ((ViewStub) findViewById(R.id.stub_map)).inflate();
//                    GoogleMapOptions mapOptions = new GoogleMapOptions();
//                    mapOptions.liteMode(true)
//                            .mapToolbarEnabled(false);
//                    mapFragment = SupportMapFragment.newInstance(mapOptions);
//                    getSupportFragmentManager()
//                            .beginTransaction()
//                            .add(R.id.panel_map, mapFragment)
//                            .commit();
//                    mapFragment.getMapAsync(callbackObject);
//                } else {
//                    updateMap(coordinates);
//                }
//            }
//
//            @Override
//            public void onError(@NonNull Status status) {
//                // TODO: Handle the error.
//                Log.i(TAG, "An error occurred: " + status);
//            }
//        });
//    }

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
                Log.i(TAG, "Place: " + place.getAddressComponents());

                coordinates = place.getLatLng();
                if (mapFragment == null) {
                    View mapPanel = ((ViewStub) findViewById(R.id.stub_map)).inflate();
                    GoogleMapOptions mapOptions = new GoogleMapOptions();
                    mapOptions.mapToolbarEnabled(false);
                    mapFragment = SupportMapFragment.newInstance(mapOptions);
                    getSupportFragmentManager()
                            .beginTransaction()
                            .add(R.id.panel_map, mapFragment)
                            .commit();
                    mapFragment.getMapAsync(callbackObject);
                } else {
                    updateMap(coordinates);
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
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
            boolean success = map.setMapStyle(new MapStyleOptions(getResources()
                    .getString(R.string.style_json)));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 15f));
        marker = map.addMarker(new MarkerOptions().position(coordinates));
    }

    private void updateMap(LatLng latLng) {
        marker.setPosition(latLng);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
    }

}
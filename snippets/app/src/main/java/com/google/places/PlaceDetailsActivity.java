package com.google.places;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Arrays;
import java.util.List;

class PlaceDetailsActivity extends AppCompatActivity {

    private static final String TAG = PlaceDetailsActivity.class.getSimpleName();

    private void simpleExamples(Place place) {
        // [START maps_places_place_details_simple]
        final CharSequence name = place.getName();
        final CharSequence address = place.getAddress();
        final LatLng location = place.getLatLng();
        // [END maps_places_place_details_simple]
    }

    private PlacesClient placesClient;

    private void getPlaceById() {
        // [START maps_places_get_place_by_id]
        // Define a Place ID.
        final String placeId = "INSERT_PLACE_ID_HERE";

        // Specify the fields to return.
        final List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME);

        // Construct a request object, passing the place ID and fields array.
        final FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            Log.i(TAG, "Place found: " + place.getName());
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                final ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Place not found: " + exception.getMessage());
                final int statusCode = apiException.getStatusCode();
                // TODO: Handle error with given status code.
            }
        });
        // [END maps_places_get_place_by_id]
    }
}

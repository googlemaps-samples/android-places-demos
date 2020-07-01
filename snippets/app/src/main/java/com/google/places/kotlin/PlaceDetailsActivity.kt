package com.google.places.kotlin

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient

class PlaceDetailsActivity : AppCompatActivity() {
    private fun simpleExamples(place: Place) {
        // [START maps_places_place_details_simple]
        val name = place.name
        val address = place.address
        val location = place.latLng
        // [END maps_places_place_details_simple]
    }

    private lateinit var placesClient: PlacesClient

    private fun getPlaceById() {
        // [START maps_places_get_place_by_id]
        // Define a Place ID.
        val placeId = "INSERT_PLACE_ID_HERE"

        // Specify the fields to return.
        val placeFields = listOf(Place.Field.ID, Place.Field.NAME)

        // Construct a request object, passing the place ID and fields array.
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response: FetchPlaceResponse ->
                val place = response.place
                Log.i(PlaceDetailsActivity.TAG, "Place found: ${place.name}")
            }.addOnFailureListener { exception: Exception ->
                if (exception is ApiException) {
                    Log.e(TAG, "Place not found: ${exception.message}")
                    val statusCode = exception.statusCode
                    TODO("Handle error with given status code")
                }
            }
        // [END maps_places_get_place_by_id]
    }

    companion object {
        private val TAG = PlaceDetailsActivity::class.java.simpleName
    }
}

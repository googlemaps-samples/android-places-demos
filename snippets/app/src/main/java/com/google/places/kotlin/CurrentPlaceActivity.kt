package com.google.places.kotlin

import android.Manifest.permission
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceLikelihood
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient

class CurrentPlaceActivity : AppCompatActivity() {

    private lateinit var placesClient: PlacesClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // [START maps_places_current_place]
        // Use fields to define the data types to return.
        val placeFields: List<Place.Field> = listOf(Place.Field.NAME)

        // Use the builder to create a FindCurrentPlaceRequest.
        val request: FindCurrentPlaceRequest = FindCurrentPlaceRequest.newInstance(placeFields)

        // Call findCurrentPlace and handle the response (first check that the user has granted permission).
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {

            val placeResponse = placesClient.findCurrentPlace(request)
            placeResponse.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val response = task.result
                    for (placeLikelihood: PlaceLikelihood in response?.placeLikelihoods
                        ?: emptyList()) {
                        Log.i(
                            TAG,
                            "Place '${placeLikelihood.place.name}' has likelihood: ${placeLikelihood.likelihood}"
                        )
                    }
                } else {
                    val exception = task.exception
                    if (exception is ApiException) {
                        Log.e(TAG, "Place not found: ${exception.statusCode}")
                    }
                }
            }
        } else {
            // A local method to request required permissions;
            // See https://developer.android.com/training/permissions/requesting
            getLocationPermission()
        }
        // [END maps_places_current_place]
    }

    private fun getLocationPermission() {
        TODO()
    }

    companion object {
        private val TAG = CurrentPlaceActivity::class.java.simpleName
    }
}
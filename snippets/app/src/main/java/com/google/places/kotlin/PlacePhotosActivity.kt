// Copyright 2023 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.places.kotlin

import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.*

class PlacePhotosActivity : AppCompatActivity() {

    private lateinit var placesClient: PlacesClient
    private lateinit var imageView: ImageView

    private fun getPlacePhoto() {
        // [START maps_places_get_place_photos]
        // Define a Place ID.
        val placeId = "INSERT_PLACE_ID_HERE"

        // Specify fields. Requests for photos must always have the PHOTO_METADATAS field.
        val fields = listOf(Place.Field.PHOTO_METADATAS)

        // Get a Place object (this example uses fetchPlace(), but you can also use findCurrentPlace())
        val placeRequest = FetchPlaceRequest.newInstance(placeId, fields)

        placesClient.fetchPlace(placeRequest)
            .addOnSuccessListener { response: FetchPlaceResponse ->
                val place = response.place

                // Get the photo metadata.
                val metada = place.photoMetadatas
                if (metada == null || metada.isEmpty()) {
                    Log.w(TAG, "No photo metadata.")
                    return@addOnSuccessListener
                }
                val photoMetadata = metada.first()

                // Get the attribution text.
                val attributions = photoMetadata?.attributions

                // Create a FetchPhotoRequest.
                val photoRequest = FetchPhotoRequest.builder(photoMetadata)
                    .setMaxWidth(500) // Optional.
                    .setMaxHeight(300) // Optional.
                    .build()
                placesClient.fetchPhoto(photoRequest)
                    .addOnSuccessListener { fetchPhotoResponse: FetchPhotoResponse ->
                        val bitmap = fetchPhotoResponse.bitmap
                        imageView.setImageBitmap(bitmap)
                    }.addOnFailureListener { exception: Exception ->
                        if (exception is ApiException) {
                            Log.e(TAG, "Place not found: " + exception.message)
                            val statusCode = exception.statusCode
                            TODO("Handle error with given status code.")
                        }
                    }
            }
        // [END maps_places_get_place_photos]
    }

    companion object {
        private val TAG = PlacePhotosActivity::class.java.simpleName
    }
}

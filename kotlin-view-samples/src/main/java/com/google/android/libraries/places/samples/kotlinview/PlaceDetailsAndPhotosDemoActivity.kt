/*
 * Copyright 2026 Google LLC
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

package com.google.android.libraries.places.samples.kotlinview

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchResolvedPhotoUriRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.samples.kotlinview.core.Demo
import com.google.android.libraries.places.samples.kotlinview.core.DemoCategory
import com.google.android.libraries.places.samples.kotlinview.databinding.ActivityPlaceDetailsAndPhotosBinding

@Demo(
    title = "Place Details & Photos",
    description = "fetchPlace, fetchPhoto, fetchResolvedPhotoUri, and AddressDescriptor",
    category = DemoCategory.DETAILS_AND_PHOTOS,
    order = 3
)
class PlaceDetailsAndPhotosDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaceDetailsAndPhotosBinding
    private lateinit var placesClient: PlacesClient
    private var lastPhotoMetadata: PhotoMetadata? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaceDetailsAndPhotosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        placesClient = Places.createClient(this)

        binding.fetchPlaceButton.setOnClickListener {
            val placeId = binding.placeIdInput.text.toString().trim()
            if (placeId.isEmpty()) {
                Toast.makeText(this, "Please enter a Place ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            fetchPlaceDetails(placeId)
        }

        binding.fetchPhotoButton.setOnClickListener {
            val metadata = lastPhotoMetadata
            if (metadata == null) {
                Toast.makeText(this, "Fetch place details first to retrieve PhotoMetadata", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            fetchPhotoBitmap(metadata)
        }

        binding.fetchResolvedUriButton.setOnClickListener {
            val metadata = lastPhotoMetadata
            if (metadata == null) {
                Toast.makeText(this, "Fetch place details first to retrieve PhotoMetadata", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            fetchResolvedPhotoUri(metadata)
        }
    }

    private fun fetchPlaceDetails(placeId: String) {
        binding.detailsTextView.text = "Fetching place details for: $placeId..."

        val fields = listOf(
            Place.Field.ID,
            Place.Field.DISPLAY_NAME,
            Place.Field.FORMATTED_ADDRESS,
            Place.Field.LOCATION,
            Place.Field.RATING,
            Place.Field.PHOTO_METADATAS,
            Place.Field.ADDRESS_DESCRIPTOR,
            Place.Field.TYPES
        )

        val request = FetchPlaceRequest.builder(placeId, fields).build()
        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val place = response.place
                val photos = place.photoMetadatas
                lastPhotoMetadata = photos?.firstOrNull()

                val sb = StringBuilder("=== PLACE DETAILS ===\n")
                sb.append("ID: ").append(place.id).append("\n")
                sb.append("Name: ").append(place.displayName).append("\n")
                sb.append("Address: ").append(place.formattedAddress).append("\n")
                sb.append("LatLng: ").append(place.location).append("\n")
                sb.append("Rating: ").append(place.rating ?: "N/A").append("\n")
                sb.append("Photos Count: ").append(photos?.size ?: 0).append("\n\n")

                place.addressDescriptor?.let { descriptor ->
                    sb.append("=== 5.3.0 ADDRESS DESCRIPTOR ===\n")
                    descriptor.areas?.let { areas ->
                        sb.append("Areas (${areas.size}):\n")
                        areas.forEach { area ->
                            sb.append("  * ").append(area.displayName).append(" (Containment: ").append(area.containment).append(")\n")
                        }
                    }
                    descriptor.landmarks?.let { landmarks ->
                        sb.append("Landmarks (${landmarks.size}):\n")
                        landmarks.forEach { lm ->
                            sb.append("  * ").append(lm.displayName).append(" (Relationship: ").append(lm.spatialRelationship).append(")\n")
                        }
                    }
                } ?: sb.append("AddressDescriptor: Not available for this place\n")

                binding.detailsTextView.text = sb.toString()
            }
            .addOnFailureListener { e ->
                binding.detailsTextView.text = "Failed to fetch place: ${e.message}"
            }
    }

    private fun fetchPhotoBitmap(metadata: PhotoMetadata) {
        binding.detailsTextView.text = "Fetching photo Bitmap..."
        val request = FetchPhotoRequest.builder(metadata)
            .setMaxWidth(800)
            .setMaxHeight(600)
            .build()

        placesClient.fetchPhoto(request)
            .addOnSuccessListener { response ->
                val bitmap = response.bitmap
                binding.photoImageView.setImageBitmap(bitmap)
                binding.detailsTextView.text = "Photo Bitmap loaded successfully! (${bitmap.width}x${bitmap.height})"
            }
            .addOnFailureListener { e ->
                binding.detailsTextView.text = "Failed to fetch photo bitmap: ${e.message}"
            }
    }

    private fun fetchResolvedPhotoUri(metadata: PhotoMetadata) {
        binding.detailsTextView.text = "Fetching Resolved Photo URI (5.3.0)..."
        val request = FetchResolvedPhotoUriRequest.builder(metadata)
            .setMaxWidth(800)
            .setMaxHeight(600)
            .build()

        placesClient.fetchResolvedPhotoUri(request)
            .addOnSuccessListener { response ->
                val uri = response.uri
                binding.photoImageView.load(uri)
                binding.detailsTextView.text = "Resolved Photo URI fetched: $uri\nImage rendered via Coil."
            }
            .addOnFailureListener { e ->
                binding.detailsTextView.text = "Failed to fetch resolved photo URI: ${e.message}"
            }
    }
}

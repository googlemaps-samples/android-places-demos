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

package com.google.android.libraries.places.samples.kotlincompose.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPhotoResponse
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchResolvedPhotoUriRequest
import com.google.android.libraries.places.api.net.FetchResolvedPhotoUriResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.samples.kotlincompose.core.Demo
import com.google.android.libraries.places.samples.kotlincompose.core.DemoCategory

@Demo(
    title = "Place Details & Photos",
    description = "Fetch Place details with AddressDescriptor, Fetch Photo bitmap, and Fetch Resolved Photo URI.",
    category = DemoCategory.PLACE_DETAILS_AND_PHOTOS,
    route = "place_details_photos"
)
class PlaceDetailsAndPhotosDemoScreen {
    @Composable
    fun Content() {
        val context = LocalContext.current
        val placesClient = remember { Places.createClient(context) }

        var placeId by remember { mutableStateOf("ChIJ3S-JXYerEmsRUcioZayKtNo") } // Sydney Opera House
        var placeDetails by remember { mutableStateOf<Place?>(null) }
        var photoBitmap by remember { mutableStateOf<Bitmap?>(null) }
        var resolvedUri by remember { mutableStateOf<Uri?>(null) }
        var isLoading by remember { mutableStateOf(false) }
        var errorText by remember { mutableStateOf<String?>(null) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = placeId,
                onValueChange = { placeId = it },
                label = { Text("Place ID") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (placeId.isBlank()) return@Button
                    isLoading = true
                    errorText = null
                    placeDetails = null
                    photoBitmap = null
                    resolvedUri = null

                    val placeFields = listOf(
                        Place.Field.ID,
                        Place.Field.DISPLAY_NAME,
                        Place.Field.FORMATTED_ADDRESS,
                        Place.Field.ADDRESS_DESCRIPTOR,
                        Place.Field.LOCATION,
                        Place.Field.RATING,
                        Place.Field.PHOTO_METADATAS
                    )
                    val fetchPlaceRequest = FetchPlaceRequest.newInstance(placeId, placeFields)
                    placesClient.fetchPlace(fetchPlaceRequest)
                        .addOnSuccessListener { response ->
                            isLoading = false
                            val place = response.place
                            placeDetails = place

                            val photoMetadata = place.photoMetadatas?.firstOrNull()
                            if (photoMetadata != null) {
                                fetchPhotoBitmap(placesClient, photoMetadata) { bitmap ->
                                    photoBitmap = bitmap
                                }
                                fetchResolvedUri(placesClient, photoMetadata) { uri ->
                                    resolvedUri = uri
                                }
                            }
                        }
                        .addOnFailureListener { exc ->
                            isLoading = false
                            errorText = exc.localizedMessage ?: "Fetch Place failed"
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Fetch Place & Photos")
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            errorText?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            placeDetails?.let { place ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = place.displayName ?: "No Name", style = MaterialTheme.typography.titleMedium)
                        Text(text = "Address: ${place.formattedAddress ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Rating: ${place.rating ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
                        Text(text = "Location: ${place.location?.latitude}, ${place.location?.longitude}", style = MaterialTheme.typography.bodySmall)

                        place.addressDescriptor?.let { descriptor ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Address Descriptor (5.3.0)", style = MaterialTheme.typography.titleSmall)
                            descriptor.landmarks?.forEach { landmark ->
                                Text(
                                    text = "• Landmark: ${landmark.displayName} (${landmark.spatialRelationship})",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            descriptor.areas?.forEach { area ->
                                Text(
                                    text = "• Area: ${area.displayName} (${area.containment})",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            photoBitmap?.let { bitmap ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Fetched Photo Bitmap (FetchPhotoRequest)", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Place Photo Bitmap",
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                    }
                }
            }

            resolvedUri?.let { uri ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Resolved Photo URI (FetchResolvedPhotoUriRequest)", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        AsyncImage(
                            model = uri,
                            contentDescription = "Resolved Photo URI",
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                    }
                }
            }
        }
    }

    private fun fetchPhotoBitmap(
        placesClient: PlacesClient,
        photoMetadata: PhotoMetadata,
        onBitmapFetched: (Bitmap) -> Unit
    ) {
        val request = FetchPhotoRequest.builder(photoMetadata)
            .setMaxWidth(800)
            .setMaxHeight(600)
            .build()
        placesClient.fetchPhoto(request)
            .addOnSuccessListener { response: FetchPhotoResponse ->
                onBitmapFetched(response.bitmap)
            }
    }

    private fun fetchResolvedUri(
        placesClient: PlacesClient,
        photoMetadata: PhotoMetadata,
        onUriFetched: (Uri) -> Unit
    ) {
        val request = FetchResolvedPhotoUriRequest.builder(photoMetadata)
            .setMaxWidth(800)
            .setMaxHeight(600)
            .build()
        placesClient.fetchResolvedPhotoUri(request)
            .addOnSuccessListener { response: FetchResolvedPhotoUriResponse ->
                response.uri?.let(onUriFetched)
            }
    }
}

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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.IsOpenRequest
import com.google.android.libraries.places.samples.kotlincompose.core.Demo
import com.google.android.libraries.places.samples.kotlincompose.core.DemoCategory

@Demo(
    title = "Place Attributes & Hours",
    description = "IsOpen status, opening hours, sub-destinations, EV charging, and accessibility options.",
    category = DemoCategory.PLACE_ATTRIBUTES_AND_HOURS,
    route = "place_attributes_hours"
)
class PlaceAttributesAndHoursDemoScreen {
    @Composable
    fun Content() {
        val context = LocalContext.current
        val placesClient = remember { Places.createClient(context) }

        var placeId by remember { mutableStateOf("ChIJ3S-JXYerEmsRUcioZayKtNo") }
        var placeDetails by remember { mutableStateOf<Place?>(null) }
        var isOpenResult by remember { mutableStateOf<Boolean?>(null) }
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (placeId.isBlank()) return@Button
                        isLoading = true
                        errorText = null
                        isOpenResult = null

                        val placeFields = listOf(
                            Place.Field.ID,
                            Place.Field.DISPLAY_NAME,
                            Place.Field.OPENING_HOURS,
                            Place.Field.PRICE_LEVEL,
                            Place.Field.PLUS_CODE,
                            Place.Field.EV_CHARGE_OPTIONS,
                            Place.Field.ACCESSIBILITY_OPTIONS,
                            Place.Field.CONTAINING_PLACES,
                            Place.Field.SUB_DESTINATIONS
                        )
                        val request = FetchPlaceRequest.newInstance(placeId, placeFields)
                        placesClient.fetchPlace(request)
                            .addOnSuccessListener { response ->
                                isLoading = false
                                placeDetails = response.place
                            }
                            .addOnFailureListener { exc ->
                                isLoading = false
                                errorText = exc.localizedMessage ?: "Fetch Place failed"
                            }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Fetch Attributes")
                }

                Button(
                    onClick = {
                        if (placeId.isBlank()) return@Button
                        isLoading = true
                        errorText = null

                        val isOpenReq = IsOpenRequest.newInstance(placeId)
                        placesClient.isOpen(isOpenReq)
                            .addOnSuccessListener { response ->
                                isLoading = false
                                isOpenResult = response.isOpen
                            }
                            .addOnFailureListener { exc ->
                                isLoading = false
                                errorText = exc.localizedMessage ?: "IsOpen evaluation failed"
                            }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Check IsOpen")
                }
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            errorText?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            isOpenResult?.let { isOpen ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "IsOpen Status (IsOpenRequest)", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isOpen) "Status: Currently Open" else "Status: Currently Closed",
                            color = if (isOpen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            placeDetails?.let { place ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = place.displayName ?: "Place Details", style = MaterialTheme.typography.titleMedium)
                        Text(text = "Price Level: ${place.priceLevel ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)

                        place.plusCode?.let { code ->
                            Text(text = "Plus Code: ${code.compoundCode ?: code.globalCode ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
                        }

                        place.openingHours?.let { hours ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Opening Hours:", style = MaterialTheme.typography.titleSmall)
                            hours.weekdayText.forEach { text ->
                                Text(text = "• $text", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        place.evChargeOptions?.let { ev ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "EV Charging Options:", style = MaterialTheme.typography.titleSmall)
                            Text(text = "• Connector Count: ${ev.connectorCount}", style = MaterialTheme.typography.bodySmall)
                        }

                        place.accessibilityOptions?.let { access ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Accessibility Attributes:", style = MaterialTheme.typography.titleSmall)
                            Text(text = "• Wheelchair Seating: ${access.wheelchairAccessibleSeating}", style = MaterialTheme.typography.bodySmall)
                            Text(text = "• Wheelchair Entrance: ${access.wheelchairAccessibleEntrance}", style = MaterialTheme.typography.bodySmall)
                            Text(text = "• Wheelchair Parking: ${access.wheelchairAccessibleParking}", style = MaterialTheme.typography.bodySmall)
                            Text(text = "• Wheelchair Restroom: ${access.wheelchairAccessibleRestroom}", style = MaterialTheme.typography.bodySmall)
                        }

                        if (!place.containingPlaces.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Containing Places:", style = MaterialTheme.typography.titleSmall)
                            place.containingPlaces?.forEach { parent ->
                                Text(text = "• Parent: ${parent.id}", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        if (!place.subDestinations.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Sub-Destinations:", style = MaterialTheme.typography.titleSmall)
                            place.subDestinations?.forEach { sub ->
                                Text(text = "• Sub-destination: ${sub.id} (${sub.name})", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

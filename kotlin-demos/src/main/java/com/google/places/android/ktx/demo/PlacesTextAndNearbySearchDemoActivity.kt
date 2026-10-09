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

package com.google.places.android.ktx.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.libraries.places.api.model.Place
import com.google.places.android.ktx.demo.ui.DemoTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Jetpack Compose activity demonstrating `awaitSearchByText`, `awaitSearchNearby`, and `awaitIsOpen`
 * via [PlacesTextAndNearbySearchViewModel], alongside Places SDK 5.3.0 rich place attributes
 * (`AddressDescriptor`, `ContainingPlace`, `EVChargeOptions`, and `AccessibilityOptions`).
 */
@AndroidEntryPoint
class PlacesTextAndNearbySearchDemoActivity : ComponentActivity() {

    private val viewModel: PlacesTextAndNearbySearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DemoTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                PlacesTextAndNearbySearchScreen(
                    uiState = uiState,
                    onSearchByText = { query, includedType, openNow ->
                        viewModel.searchByText(
                            query = query,
                            includedType = includedType,
                            openNow = openNow
                        )
                    },
                    onSearchNearby = { includedType ->
                        viewModel.searchNearby(includedType = includedType)
                    },
                    onPlaceSelected = { place ->
                        viewModel.selectPlaceAndCheckOpenStatus(place)
                    },
                    onBackPressed = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesTextAndNearbySearchScreen(
    uiState: TextAndNearbySearchUiState,
    onSearchByText: (query: String, includedType: String, openNow: Boolean) -> Unit,
    onSearchNearby: (includedType: String) -> Unit,
    onPlaceSelected: (Place) -> Unit,
    onBackPressed: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("Spicy Vegetarian Food in Sydney") }
    var includedType by rememberSaveable { mutableStateOf("restaurant") }
    var openNow by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Text & Nearby Search (KTX)") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Text Search Query") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = includedType,
                    onValueChange = { includedType = it },
                    label = { Text("Included Type") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = openNow,
                        onCheckedChange = { openNow = it }
                    )
                    Text("Open Now", style = MaterialTheme.typography.bodySmall)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onSearchByText(query, includedType, openNow) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("awaitSearchByText")
                }
                Button(
                    onClick = { onSearchNearby(includedType) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("awaitSearchNearby")
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            uiState.selectedPlace?.let { selected ->
                SelectedPlaceAttributesCard(
                    place = selected,
                    isOpenStatus = uiState.isOpenStatus
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.places) { place ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPlaceSelected(place) }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = place.displayName ?: "Unnamed Place",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = place.formattedAddress ?: "No formatted address",
                                style = MaterialTheme.typography.bodySmall
                            )
                            place.rating?.let { rating ->
                                Text(
                                    text = "Rating: $rating",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedPlaceAttributesCard(
    place: Place,
    isOpenStatus: Boolean?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Selected Place Attributes: ${place.displayName ?: place.id}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            isOpenStatus?.let { isOpen ->
                Text(
                    text = "awaitIsOpen Status: ${if (isOpen) "Open" else "Closed"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            place.addressDescriptor?.let { descriptor ->
                val landmarkSummary = descriptor.landmarks?.firstOrNull()?.let {
                    "${it.displayName} (${it.spatialRelationship})"
                } ?: "None"
                Text(
                    text = "AddressDescriptor Landmark: $landmarkSummary",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            place.evChargeOptions?.let { ev ->
                Text(
                    text = "EV Connectors: ${ev.connectorCount}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            place.accessibilityOptions?.let { access ->
                Text(
                    text = "Wheelchair Entrance: ${access.wheelchairAccessibleEntrance}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (!place.containingPlaces.isNullOrEmpty()) {
                Text(
                    text = "Containing Places: ${place.containingPlaces?.size}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}

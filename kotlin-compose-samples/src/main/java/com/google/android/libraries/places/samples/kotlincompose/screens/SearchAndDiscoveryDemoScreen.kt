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

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.google.android.libraries.places.samples.kotlincompose.core.Demo
import com.google.android.libraries.places.samples.kotlincompose.core.DemoCategory

@Demo(
    title = "Search & Discovery",
    description = "Text Search, Nearby Search, Find Current Place, and Autocomplete predictions.",
    category = DemoCategory.SEARCH_AND_DISCOVERY,
    route = "search_discovery"
)
class SearchAndDiscoveryDemoScreen {
    @Composable
    fun Content() {
        val context = LocalContext.current
        val placesClient = remember { Places.createClient(context) }
        var selectedTabIndex by remember { mutableIntStateOf(0) }
        val tabs = listOf("SearchByText", "SearchNearby", "CurrentPlace", "Autocomplete")

        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> SearchByTextTab(placesClient)
                    1 -> SearchNearbyTab(placesClient)
                    2 -> FindCurrentPlaceTab(placesClient)
                    3 -> AutocompleteTab(placesClient)
                }
            }
        }
    }

    @Composable
    private fun SearchByTextTab(placesClient: PlacesClient) {
        var query by remember { mutableStateOf("Pizza in Sydney") }
        var results by remember { mutableStateOf<List<Place>>(emptyList()) }
        var isLoading by remember { mutableStateOf(false) }
        var errorText by remember { mutableStateOf<String?>(null) }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search Query") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    if (query.isBlank()) return@Button
                    isLoading = true
                    errorText = null
                    val placeFields = listOf(
                        Place.Field.ID,
                        Place.Field.DISPLAY_NAME,
                        Place.Field.FORMATTED_ADDRESS,
                        Place.Field.LOCATION,
                        Place.Field.RATING
                    )
                    val request = SearchByTextRequest.builder(query, placeFields).build()
                    placesClient.searchByText(request)
                        .addOnSuccessListener { response ->
                            isLoading = false
                            results = response.places
                        }
                        .addOnFailureListener { exception ->
                            isLoading = false
                            errorText = exception.localizedMessage ?: "Search failed"
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Search By Text")
            }

            DisplayResults(isLoading, errorText, results) { place ->
                "${place.displayName}\n${place.formattedAddress ?: "No address"}\nRating: ${place.rating ?: "N/A"}"
            }
        }
    }

    @Composable
    private fun SearchNearbyTab(placesClient: PlacesClient) {
        var results by remember { mutableStateOf<List<Place>>(emptyList()) }
        var isLoading by remember { mutableStateOf(false) }
        var errorText by remember { mutableStateOf<String?>(null) }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Search Nearby (Sydney Opera House center, 1000m radius)",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(
                onClick = {
                    isLoading = true
                    errorText = null
                    val center = LatLng(-33.8567844, 151.2152967)
                    val circle = CircularBounds.newInstance(center, 1000.0)
                    val placeFields = listOf(
                        Place.Field.ID,
                        Place.Field.DISPLAY_NAME,
                        Place.Field.FORMATTED_ADDRESS,
                        Place.Field.RATING,
                        Place.Field.TYPES
                    )
                    val request = SearchNearbyRequest.builder(circle, placeFields)
                        .setIncludedTypes(listOf("restaurant", "cafe"))
                        .setMaxResultCount(10)
                        .build()
                    placesClient.searchNearby(request)
                        .addOnSuccessListener { response ->
                            isLoading = false
                            results = response.places
                        }
                        .addOnFailureListener { exception ->
                            isLoading = false
                            errorText = exception.localizedMessage ?: "Nearby search failed"
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Execute Nearby Search")
            }

            DisplayResults(isLoading, errorText, results) { place ->
                "${place.displayName}\n${place.formattedAddress ?: "No address"}\nTypes: ${place.placeTypes?.take(3)?.joinToString()}"
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Composable
    private fun FindCurrentPlaceTab(placesClient: PlacesClient) {
        var statusMessage by remember { mutableStateOf<String?>(null) }
        var likelihoods by remember { mutableStateOf<List<Pair<String, Double>>>(emptyList()) }
        var isLoading by remember { mutableStateOf(false) }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Find Current Place detects likelihoods based on location permissions.",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(
                onClick = {
                    isLoading = true
                    statusMessage = null
                    val placeFields = listOf(Place.Field.DISPLAY_NAME, Place.Field.FORMATTED_ADDRESS, Place.Field.TYPES)
                    val request = FindCurrentPlaceRequest.newInstance(placeFields)
                    placesClient.findCurrentPlace(request)
                        .addOnSuccessListener { response ->
                            isLoading = false
                            likelihoods = response.placeLikelihoods.map {
                                Pair(it.place.displayName ?: "Unknown", it.likelihood)
                            }
                        }
                        .addOnFailureListener { exception ->
                            isLoading = false
                            statusMessage = "Error: ${exception.localizedMessage}"
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Find Current Place")
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            statusMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(likelihoods) { (name, likelihood) ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = name, style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "Likelihood: ${"%.2f".format(likelihood * 100)}%",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun AutocompleteTab(placesClient: PlacesClient) {
        var query by remember { mutableStateOf("Opera") }
        var predictions by remember { mutableStateOf<List<String>>(emptyList()) }
        var isLoading by remember { mutableStateOf(false) }
        var errorText by remember { mutableStateOf<String?>(null) }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    if (it.length >= 2) {
                        isLoading = true
                        val request = FindAutocompletePredictionsRequest.builder()
                            .setQuery(it)
                            .build()
                        placesClient.findAutocompletePredictions(request)
                            .addOnSuccessListener { response ->
                                isLoading = false
                                predictions = response.autocompletePredictions.map { pred ->
                                    "${pred.getPrimaryText(null)} - ${pred.getSecondaryText(null)}"
                                }
                            }
                            .addOnFailureListener { exc ->
                                isLoading = false
                                errorText = exc.localizedMessage
                            }
                    }
                },
                label = { Text("Autocomplete Query") },
                modifier = Modifier.fillMaxWidth()
            )

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            errorText?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(predictions) { prediction ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = prediction,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun <T> DisplayResults(
        isLoading: Boolean,
        errorText: String?,
        results: List<T>,
        formatter: (T) -> String
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }
        errorText?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(results) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = formatter(item),
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

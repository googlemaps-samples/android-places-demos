// Copyright 2026 Google LLC
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

package com.google.places.android.ktx.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.places.android.ktx.demo.ui.DemoTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * A demo activity showcasing the use of the Places KTX library for photo fetching.
 *
 * This activity demonstrates:
 * 1. Searching for places using [PlacesPhotoViewModel.searchResults].
 * 2. Fetching place details to obtain photo metadata.
 * 3. Resolving a photo URI using [com.google.android.libraries.places.api.net.kotlin.awaitFetchResolvedPhotoUri].
 * 4. Displaying the resolved photo URI using the Coil library.
 *
 * The UI is built using Jetpack Compose and follows a standard MVI-lite pattern with a ViewModel
 * exposing state via [StateFlow].
 */
@AndroidEntryPoint
class PlacesPhotoDemoActivity : ComponentActivity() {

    private val viewModel: PlacesPhotoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DemoTheme {
                PlacesPhotoScreen(
                    viewModel = viewModel,
                    onBackPressed = { finish() }
                )
            }
        }
    }
}

/**
 * The main screen for the Places Photo Demo.
 *
 * This composable manages the high-level state of the screen, switching between a search result
 * list and a detailed photo display.
 *
 * @param viewModel The ViewModel providing state and handling interactions.
 * @param onBackPressed Callback for when the user wants to navigate back.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesPhotoScreen(
    viewModel: PlacesPhotoViewModel,
    onBackPressed: () -> Unit
) {
    // Collect the search results and photo state from the ViewModel.
    // Using collectAsStateWithLifecycle ensures that collection stops when the app is in the background.
    val searchEvent by viewModel.searchResults.collectAsStateWithLifecycle()
    val photoState by viewModel.photoState.collectAsStateWithLifecycle()
    
    // searchQuery is local UI state used only for the text field input.
    var searchQuery by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Places Photo Demo") },
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
        ) {
            // Search Input: Real-time search with a debounce applied in the ViewModel.
            TextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.onSearchQueryChanged(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search for a place with photos...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { 
                        searchQuery = ""
                        viewModel.searchNearby() 
                    }) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Search Nearby",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                shape = MaterialTheme.shapes.medium,
                singleLine = true
            )

            // Content Area: Switch between search results and the photo display based on state.
            Box(modifier = Modifier.fillMaxSize()) {
                if (photoState.uri != null || photoState.isLoading || photoState.error != null) {
                    // When a photo is being fetched or displayed, show the PhotoDisplay.
                    PhotoDisplay(
                        state = photoState,
                        onBackPressed = { viewModel.onSearchQueryChanged(searchQuery) }
                    )
                } else {
                    // Otherwise, show the interactive list of autocomplete predictions.
                    SearchResultsList(
                        event = searchEvent,
                        onSearchNearbyClick = { 
                            searchQuery = ""
                            viewModel.searchNearby() 
                        },
                        onPredictionClick = { viewModel.onPredictionClicked(it) }
                    )
                }
            }
        }
    }
}

/**
 * A prominent informational section shown when the app is in the idle state.
 *
 * This "Hero" card explicitly demonstrates that [com.google.android.libraries.places.api.net.kotlin.awaitSearchNearby]
 * is the modern, recommended replacement for the deprecated [PlacesClient.findCurrentPlace] API.
 */
@Composable
fun SearchNearbyHero(onSearchNearbyClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(
                    "Need Nearby Places?",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "The legacy 'findCurrentPlace' API has been removed. " +
                    "Use 'searchNearby' with explicit location bounds instead.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(Modifier.height(24.dp))
                
                Button(
                    onClick = onSearchNearbyClick,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Search Near Googleplex")
                }
            }
        }
        
        Spacer(Modifier.height(32.dp))
        
        Text(
            "Or search for a specific place above",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Displays a list of autocomplete predictions as search results.
 *
 * @param event The current [PhotoDemoEvent] from the ViewModel.
 * @param onPredictionClick Callback when a prediction is tapped.
 */
@Composable
fun SearchResultsList(
    event: PhotoDemoEvent,
    onSearchNearbyClick: () -> Unit,
    onPredictionClick: (AutocompletePrediction) -> Unit
) {
    when (event) {
        is PhotoDemoEventIdle -> {
            SearchNearbyHero(onSearchNearbyClick)
        }
        is PhotoDemoEventLoading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is PhotoDemoEventResults -> {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(event.predictions) { prediction ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPredictionClick(prediction) }
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(prediction.getPrimaryText(null).toString(), fontWeight = FontWeight.Bold)
                            Text(prediction.getSecondaryText(null).toString(), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        is PhotoDemoEventError -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: ${event.exception.message}", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

/**
 * Displays the resolved photo URI or the loading/error state during resolution.
 *
 * This component showcases the integration with Coil's [AsyncImage] to display the photo
 * once the URI is successfully fetched.
 *
 * @param state The current [PhotoState] containing the URI, loading status, or error.
 * @param onBackPressed Callback to return to search results.
 */
@Composable
fun PhotoDisplay(
    state: PhotoState,
    onBackPressed: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (state.isLoading) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text("Resolving Photo URI...")
        } else if (state.error != null) {
            Text("Error", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.error)
            Text(state.error)
            Button(onClick = onBackPressed, Modifier.padding(top = 16.dp)) {
                Text("Back to Results")
            }
        } else if (state.uri != null) {
            Text("Fetched Photo URI", style = MaterialTheme.typography.headlineSmall)
            Text(
                text = state.uri.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(8.dp)
            )
            
            Spacer(Modifier.height(16.dp))
            
            // This is the core of the demo: Using Coil's AsyncImage with the fetched URI.
            // Coil handles the network fetching and caching of the actual image data from the URI.
            AsyncImage(
                model = state.uri,
                contentDescription = "Place Photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color.LightGray, MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )

            Button(onClick = onBackPressed, Modifier.padding(top = 24.dp)) {
                Text("Search Another Photo")
            }
        }
    }
}

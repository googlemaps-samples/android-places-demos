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

import android.graphics.Typeface
import android.os.Bundle
import android.text.style.StyleSpan
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.kotlin.awaitFetchPlace
import com.google.android.libraries.places.widget.PlaceAutocomplete
import com.google.android.libraries.places.widget.PlaceAutocompleteActivity
import kotlinx.coroutines.launch
import com.google.places.android.ktx.demo.ui.DemoTheme

class AutocompleteDemoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DemoTheme {
                AutocompleteDemoScreen(onBackPressed = { finish() })
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun AutocompleteDemoScreen(onBackPressed: () -> Unit) {
        val placesClient = remember { Places.createClient(this) }
        val scope = rememberCoroutineScope()
        var placeDetails by remember { mutableStateOf<String?>(null) }
        var isFetching by remember { mutableStateOf(false) }

        val startAutocomplete =
            rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == RESULT_OK) {
                    val intent = result.data
                    if (intent != null) {
                        val prediction = PlaceAutocomplete.getPredictionFromIntent(intent)
                        if (prediction != null) {
                            placeDetails = "Fetching details for '${prediction.getPrimaryText(null)}'..."
                            isFetching = true

                            // Modern Pattern: Use the widget to get a prediction, then fetch full details via KTX
                            scope.launch {
                                try {
                                    val fields = listOf(Place.Field.DISPLAY_NAME, Place.Field.FORMATTED_ADDRESS)
                                    val response = placesClient.awaitFetchPlace(prediction.placeId, fields)
                                    val place = response.place
                                    placeDetails = "Got place '${place.displayName}' (${place.formattedAddress})"
                                } catch (e: Exception) {
                                    placeDetails = "Error fetching details: ${e.message}"
                                } finally {
                                    isFetching = false
                                }
                            }
                        }
                    }
                } else if (result.resultCode == PlaceAutocompleteActivity.RESULT_ERROR) {
                    val intent = result.data
                    if (intent != null) {
                        val status = PlaceAutocomplete.getResultStatusFromIntent(intent)
                        Toast.makeText(
                            this,
                            "Autocomplete error: ${status?.statusMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Autocomplete Demo") },
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
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = placeDetails ?: "No place selected yet",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = when {
                                isFetching -> MaterialTheme.colorScheme.secondary
                                placeDetails != null -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                val fields = listOf(Place.Field.ID, Place.Field.DISPLAY_NAME, Place.Field.FORMATTED_ADDRESS)
                                val intent = PlaceAutocomplete.createIntent(this@AutocompleteDemoActivity) {
                                    // Specify any builder parameters here (e.g. setCountries, setLocationBias, etc.)
                                }
                                startAutocomplete.launch(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("Start Autocomplete")
                        }
                    }
                }
            }
        }
    }
}
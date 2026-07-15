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

package com.google.android.libraries.places.samples.kotlincompose

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.samples.kotlincompose.core.DemoCategory
import com.google.android.libraries.places.samples.kotlincompose.core.DemoRegistry
import com.google.android.libraries.places.samples.kotlincompose.screens.CoreInitializationDemoScreen
import com.google.android.libraries.places.samples.kotlincompose.screens.PlaceAttributesAndHoursDemoScreen
import com.google.android.libraries.places.samples.kotlincompose.screens.PlaceDetailsAndPhotosDemoScreen
import com.google.android.libraries.places.samples.kotlincompose.screens.PlacesUIKitAndActionsDemoScreen
import com.google.android.libraries.places.samples.kotlincompose.screens.SearchAndDiscoveryDemoScreen

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Places SDK with API key if configured
        val apiKey = BuildConfig.PLACES_API_KEY
        if (apiKey.isNotEmpty() && apiKey != "YOUR_API_KEY") {
            Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)
        } else {
            Log.w("MainActivity", "No valid PLACES_API_KEY set in local.properties")
            Toast.makeText(this, "Set PLACES_API_KEY in local.properties", Toast.LENGTH_LONG).show()
        }

        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    var currentTitle by remember { mutableStateOf("Places SDK Compose Catalog") }
                    var isMenuRoute by remember { mutableStateOf(true) }

                    Scaffold(
                        topBar = {
                            MainTopAppBar(
                                title = currentTitle,
                                showBackButton = !isMenuRoute,
                                onBackClicked = {
                                    navController.popBackStack()
                                    isMenuRoute = true
                                    currentTitle = "Places SDK Compose Catalog"
                                }
                            )
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = "menu",
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable("menu") {
                                isMenuRoute = true
                                currentTitle = "Places SDK Compose Catalog"
                                DemoMenuScreen(onDemoSelected = { route, title ->
                                    isMenuRoute = false
                                    currentTitle = title
                                    navController.navigate(route)
                                })
                            }
                            composable("core_init") {
                                CoreInitializationDemoScreen().Content()
                            }
                            composable("search_discovery") {
                                SearchAndDiscoveryDemoScreen().Content()
                            }
                            composable("place_details_photos") {
                                PlaceDetailsAndPhotosDemoScreen().Content()
                            }
                            composable("place_attributes_hours") {
                                PlaceAttributesAndHoursDemoScreen().Content()
                            }
                            composable("uikit_actions") {
                                PlacesUIKitAndActionsDemoScreen().Content()
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    title: String,
    showBackButton: Boolean,
    onBackClicked: () -> Unit
) {
    TopAppBar(
        title = { Text(text = title, style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClicked) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
fun DemoMenuScreen(onDemoSelected: (String, String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredDemos = remember(searchQuery) { DemoRegistry.searchDemos(searchQuery) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Filter API Demos") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier.fillMaxWidth()
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            DemoCategory.entries.forEach { category ->
                val categoryDemos = filteredDemos.filter { it.category == category }
                if (categoryDemos.isNotEmpty()) {
                    item {
                        Text(
                            text = category.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                    items(categoryDemos) { demo ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onDemoSelected(demo.route, demo.title) },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = demo.title,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = demo.description,
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

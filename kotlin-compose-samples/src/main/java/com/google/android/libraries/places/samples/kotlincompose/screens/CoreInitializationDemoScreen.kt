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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.samples.kotlincompose.BuildConfig
import com.google.android.libraries.places.samples.kotlincompose.core.Demo
import com.google.android.libraries.places.samples.kotlincompose.core.DemoCategory

@Demo(
    title = "Core Initialization & App Check",
    description = "Initialize Places SDK 5.3.0, check status, inspect App Check, and deinitialize.",
    category = DemoCategory.CORE_INITIALIZATION,
    route = "core_init"
)
class CoreInitializationDemoScreen {
    @Composable
    fun Content() {
        val context = LocalContext.current
        var isInitialized by remember { mutableStateOf(Places.isInitialized()) }
        var logs by remember { mutableStateOf(listOf("SDK Initialized State: ${Places.isInitialized()}")) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Places SDK Status",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isInitialized) "STATUS: INITIALIZED (5.3.0)" else "STATUS: NOT INITIALIZED",
                        color = if (isInitialized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Actions",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val apiKey = BuildConfig.PLACES_API_KEY
                                if (apiKey.isNotEmpty() && apiKey != "YOUR_API_KEY") {
                                    Places.initializeWithNewPlacesApiEnabled(context.applicationContext, apiKey)
                                    isInitialized = Places.isInitialized()
                                    logs = logs + "Executed Places.initializeWithNewPlacesApiEnabled()"
                                } else {
                                    logs = logs + "Error: Valid PLACES_API_KEY required in local.properties"
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Initialize")
                        }
                        Button(
                            onClick = {
                                Places.deinitialize()
                                isInitialized = Places.isInitialized()
                                logs = logs + "Executed Places.deinitialize()"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Deinitialize")
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "App Check & Attestation Provider",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Places SDK 5.3.0 natively validates request authenticity using SafetyNet / Play Integrity App Check provider integration. Tokens are attached to outbound API calls when initialized with App Check providers.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Activity Log",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    logs.forEach { log ->
                        Text(text = "• $log", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

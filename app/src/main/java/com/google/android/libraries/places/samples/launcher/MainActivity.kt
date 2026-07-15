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

package com.google.android.libraries.places.samples.launcher

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.IntegrationInstructions
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PhonelinkSetup
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LauncherDashboardScreen()
                }
            }
        }
    }
}

data class SampleApp(
    val title: String,
    val subtitle: String,
    val packageName: String,
    val category: String,
    val icon: ImageVector,
    val tag: String
)

val SAMPLES = listOf(
    // 🌟 Redesigned Core Sample Suites
    SampleApp(
        title = "Kotlin Compose Samples",
        subtitle = "Jetpack Compose + Material 3 + Navigation",
        packageName = "com.google.android.libraries.places.samples.kotlincompose",
        category = "🌟 Redesigned Core Sample Suites (v5.3.0)",
        icon = Icons.Default.Star,
        tag = "RECOMMENDED"
    ),
    SampleApp(
        title = "Kotlin View Samples",
        subtitle = "Kotlin + Android Views + ViewBinding + KTX",
        packageName = "com.google.android.libraries.places.samples.kotlinview",
        category = "🌟 Redesigned Core Sample Suites (v5.3.0)",
        icon = Icons.Default.PhonelinkSetup,
        tag = "KOTLIN"
    ),
    SampleApp(
        title = "Java View Samples",
        subtitle = "Pure Java + Android Views + Material 3",
        packageName = "com.google.android.libraries.places.samples.javaview",
        category = "🌟 Redesigned Core Sample Suites (v5.3.0)",
        icon = Icons.Default.Code,
        tag = "JAVA"
    ),

    // 🚀 Specialized UI Kit & 3D Demos
    SampleApp(
        title = "Places UI Kit 3D Map Flyover",
        subtitle = "Photorealistic 3D Maps SDK + UI Kit integration",
        packageName = "com.example.placesuikit3d",
        category = "🚀 Specialized UI Kit & 3D Demos",
        icon = Icons.Default.Map,
        tag = "3D MAPS"
    ),
    SampleApp(
        title = "Place Details Compose",
        subtitle = "Compact & Full UI Kit Fragments in Compose",
        packageName = "com.example.placedetailscompose",
        category = "🚀 Specialized UI Kit & 3D Demos",
        icon = Icons.Default.Widgets,
        tag = "COMPOSE"
    ),
    SampleApp(
        title = "Place Details UI Kit",
        subtitle = "Interactive field customization & compact widgets",
        packageName = "com.example.placedetailsuikit",
        category = "🚀 Specialized UI Kit & 3D Demos",
        icon = Icons.Default.IntegrationInstructions,
        tag = "UI KIT"
    ),

    // 📦 Base Demos & Utilities
    SampleApp(
        title = "Base Kotlin Demos",
        subtitle = "Standard Places SDK feature demos in Kotlin",
        packageName = "com.example.placesdemo.kotlin",
        category = "📦 Base Demos & Developer Utility Suites",
        icon = Icons.Default.PlayArrow,
        tag = "BASE"
    ),
    SampleApp(
        title = "Base Java Demos",
        subtitle = "Standard Places SDK feature demos in Java",
        packageName = "com.example.placesdemo",
        category = "📦 Base Demos & Developer Utility Suites",
        icon = Icons.Default.PlayArrow,
        tag = "BASE"
    ),
    SampleApp(
        title = "Kotlin Coroutines KTX Demos",
        subtitle = "Asynchronous flows using android-places-ktx",
        packageName = "com.google.maps.android.ktx.demo",
        category = "📦 Base Demos & Developer Utility Suites",
        icon = Icons.Default.PlayArrow,
        tag = "KTX"
    ),
    SampleApp(
        title = "Code Snippets Suite",
        subtitle = "Developer documentation code snippet samples",
        packageName = "com.google.android.libraries.places.snippets",
        category = "📦 Base Demos & Developer Utility Suites",
        icon = Icons.Default.Code,
        tag = "SNIPPETS"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LauncherDashboardScreen() {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "📍 Places SDK Master Launcher",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header Info Banner
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Places SDK for Android v5.3.0",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Select any sample suite below to launch its dedicated application target directly.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Group by category
            val grouped = SAMPLES.groupBy { it.category }
            grouped.forEach { (category, apps) ->
                Text(
                    text = category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                apps.forEach { app ->
                    SampleAppCard(app = app, onLaunch = { launchTargetApp(context, app.packageName) })
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun SampleAppCard(app: SampleApp, onLaunch: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = app.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = app.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onLaunch,
                contentPadding = ButtonDefaults.ContentPadding
            ) {
                Icon(
                    imageVector = Icons.Default.Launch,
                    contentDescription = "Launch",
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(text = "Launch", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

fun launchTargetApp(context: Context, packageName: String) {
    val intent = context.packageManager.getLaunchIntentForPackage(packageName)
    if (intent != null) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } else {
        Toast.makeText(
            context,
            "App target '$packageName' is not installed.",
            Toast.LENGTH_LONG
        ).show()
    }
}

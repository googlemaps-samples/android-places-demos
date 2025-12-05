// Copyright 2025 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.placedetailscompose

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.placedetailscompose.ui.map.MapScreen
import com.example.placedetailscompose.ui.theme.PlaceDetailsComposeTheme
import com.google.android.libraries.places.api.Places
import com.google.maps.android.compose.internal.InitializationState
import com.google.maps.android.compose.internal.LocalGoogleMapsInitializer

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve the API key from the local.properties file.
        // See https://github.com/googlemaps/android-places-demos#installation for more details.
        val apiKey = BuildConfig.PLACES_API_KEY
        if (apiKey.isEmpty() || apiKey == "YOUR_API_KEY") {
            Log.e("PlacesCompose", "No api key")
            Toast.makeText(
                this,
                "Add your own API_KEY in local.properties",
                Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        // Initialize the Places SDK. This must be done before calling any other Places API methods.
        // The 'newPlacesApiEnabled' flag indicates that the new Places API should be used.
        // This can happen in an Activity or the Application.
        Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)

        enableEdgeToEdge()
        setContent {
            val googleMapsInitializer = LocalGoogleMapsInitializer.current
            val initializationState by googleMapsInitializer.state
            val window = this.window
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)

            SideEffect {
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }

            if (initializationState != InitializationState.SUCCESS) {
                val context = LocalContext.current
                LaunchedEffect(Unit) {
                    googleMapsInitializer.initialize(context)
                }
            }

            PlaceDetailsComposeTheme {
                if (initializationState == InitializationState.SUCCESS) {
                    MapScreen()
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}
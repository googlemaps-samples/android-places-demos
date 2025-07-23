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
import com.example.placedetailscompose.ui.map.MapScreen
import com.example.placedetailscompose.ui.theme.PlaceDetailsComposeTheme
import com.google.android.libraries.places.api.Places

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
        Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)

        enableEdgeToEdge()
        setContent {
            PlaceDetailsComposeTheme {
                MapScreen()
            }
        }
    }
}
// Copyright 2025 Google LLC
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

package com.google.places.kotlin

import android.app.Application
import android.util.Log
import android.widget.Toast
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.places.BuildConfig

class MainApplication : Application() {
    private lateinit var placesClient: PlacesClient

    override fun onCreate() {
        super.onCreate()

        val apiKey = BuildConfig.PLACES_API_KEY
        if (apiKey == "DEFAULT_API_KEY") {
            Toast.makeText(this, "PLACES_API_KEY has not been configured", Toast.LENGTH_SHORT).show()
            Log.e("GetStartedActivity", "PLACES_API_KEY has not been configured. See app/build.gradle.kts")
            return
        }

        // [START maps_places_get_started]
        // Initialize the SDK
        Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)

        // Create a new PlacesClient instance
        placesClient = Places.createClient(this)
        // [END maps_places_get_started]
    }

    fun getPlacesClient(): PlacesClient {
        return placesClient
    }
}

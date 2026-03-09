// Copyright 2020 Google LLC
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

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.libraries.places.api.Places
import com.google.places.BuildConfig
import com.google.places.R

class GetStartedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val apiKey = BuildConfig.PLACES_API_KEY
        if (apiKey == "DEFAULT_API_KEY") {
            Toast.makeText(this, "PLACES_API_KEY has not been configured", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // [START maps_places_get_started]
        // Initialize the SDK
        Places.initialize(applicationContext, apiKey)

        // Create a new PlacesClient instance
        val placesClient = Places.createClient(this)
        // [END maps_places_get_started]
    }
}

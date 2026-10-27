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

package com.google.places;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.common_ui.catalog.Complexity;
import com.example.common_ui.catalog.Framework;
import com.example.common_ui.catalog.Sample;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

@Sample(
        id = "com.google.places.kotlin.GetStartedActivity",
        title = "Snippet: Get Started & Initialize SDK",
        description =
                "DevSite documentation snippet showing Places.initializeWithNewPlacesApiEnabled and Places.createClient.",
        category = "Documentation Snippets",
        complexity = Complexity.SNIPPET,
        tags = {"#snippet", "#init", "#getstarted", "#placesclient"},
        apiCalls = {
            "Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)",
            "Places.createClient(context)"
        },
        purpose =
                "Canonical DevSite documentation snippet demonstrating how to initialize the Places SDK with the New Places API enabled and create a PlacesClient instance.",
        successCriteria =
                "• Activity launches cleanly, initializes Places SDK with the configured API key, and creates a PlacesClient without error.",
        failureIndicators = "• Activity crashes or shows an unconfigured API key toast.",
        framework = Framework.JAVA_VIEWS)
public class GetStartedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String apiKey = BuildConfig.PLACES_API_KEY;
        if (apiKey.equals("DEFAULT_API_KEY")) {
            Toast.makeText(this, "PLACES_API_KEY has not been configured", Toast.LENGTH_SHORT)
                    .show();
            finish();
            return;
        }

        // [START maps_places_get_started]
        // Initialize the SDK
        Places.initializeWithNewPlacesApiEnabled(getApplicationContext(), apiKey);

        // Create a new PlacesClient instance
        PlacesClient placesClient = Places.createClient(this);
        // [END maps_places_get_started]
    }
}

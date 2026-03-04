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

import android.app.Application
import com.google.android.libraries.places.api.Places
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize the Places SDK. Note that the string value of `maps_api_key` will be generated
        // at build-time (see app/build.gradle). The technique used here allows you to provide your
        // API key such that the key is not checked in source control.
        //
        // See API Key Best Practices for more information on how to secure your API key:
        // https://developers.google.com/maps/api-key-best-practices
        // Initialize the Places SDK with the new API engine enabled
        Places.initializeWithNewPlacesApiEnabled(this, BuildConfig.PLACES_API_KEY)
    }
}

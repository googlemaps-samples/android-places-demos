/*
 * Copyright 2025 Google LLC
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

package com.example.placesdemo;

import android.app.Application;
import android.widget.Toast;

import com.google.android.libraries.places.api.Places;

public class PlacesDemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        final String apiKey = BuildConfig.PLACES_API_KEY;

        if (apiKey.equals("")) {
            Toast.makeText(this, getString(R.string.error_api_key), Toast.LENGTH_LONG).show();
            return;
        }

        Places.initialize(getApplicationContext(), apiKey);
    }
}

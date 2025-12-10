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

package com.example.placedetailscompose

import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import java.util.Objects
import kotlin.text.isBlank

/**
 * `PlaceDetailsComposeApplication` is a custom Application class.
 *
 * This class is responsible for application-wide initialization and setup,
 * such as checking for the presence and validity of the API key during the
 * application's startup.
 *
 * It extends the [Application] class and overrides the [.onCreate]
 * method to perform these initialization tasks.
 */
class PlaceDetailsComposeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        checkApiKey()
    }

    /**
     * Checks if the API key for Google Maps is properly configured in the application's metadata.
     *
     * This method retrieves the API key from the application's metadata, specifically looking for
     * a string value associated with the key "com.google.android.geo.API_KEY".
     * The key must be present, not blank, and not set to the placeholder value "DEFAULT_API_KEY".
     *
     * If any of these checks fail, a Toast message is displayed indicating that the API key is missing or
     * incorrectly configured, and a RuntimeException is thrown.
     */
    private fun checkApiKey() {
        try {
            val appInfo =
                packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            val bundle = Objects.requireNonNull(appInfo.metaData)

            val apiKey =
                bundle.getString("com.google.android.geo.API_KEY") // Key name is important!

            if (apiKey == null || apiKey.isBlank() || apiKey == "DEFAULT_API_KEY") {
                Toast.makeText(
                    this,
                    getString(R.string.error_api_key_missing),
                    Toast.LENGTH_LONG
                ).show()
                throw RuntimeException(getString(R.string.error_api_key_missing))
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Package name not found.", e)
            throw RuntimeException("Error getting package info.", e)
        } catch (e: NullPointerException) {
            Log.e(TAG, "Error accessing meta-data.", e) // Handle the case where meta-data is completely missing.
            throw RuntimeException("Error accessing meta-data in manifest", e)
        }
    }

    /**
     * Retrieves the map ID from the BuildConfig or string resource.
     *
     * @return The valid map ID or null if no valid map ID is found.
     */
    val mapId: String? by lazy {
        if (BuildConfig.MAP_ID != "YOUR_MAP_ID") {
            BuildConfig.MAP_ID
        } else if (getString(R.string.map_id) != "YOUR_MAP_ID") {
            getString(R.string.map_id)
        } else {
            Log.w(TAG, "Map ID is not set. See README for instructions.")
            Toast.makeText(this, getString(R.string.error_map_id_missing), Toast.LENGTH_LONG)
                .show()
            null
        }
    }

    companion object {
        private const val TAG = "ApiDemoApplication"
    }
}
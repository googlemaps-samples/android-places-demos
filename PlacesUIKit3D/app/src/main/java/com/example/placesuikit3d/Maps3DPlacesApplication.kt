package com.example.placesuikit3d

import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import com.google.android.libraries.places.api.Places
import dagger.hilt.android.HiltAndroidApp
import java.util.Objects

@HiltAndroidApp
class Maps3DPlacesApplication : Application() {
    val TAG = this::class.java.simpleName

    override fun onCreate() {
        super.onCreate()
        checkApiKey()
        initializePlaces()
    }

    private fun initializePlaces() {
        val apiKey = BuildConfig.PLACES_API_KEY

        if (apiKey == null || apiKey.isBlank() || apiKey == "DEFAULT_API_KEY") {
            Toast.makeText(
                this,
                "PLACES_API_KEY was not set in secrets.properties",
                Toast.LENGTH_LONG
            ).show()
            throw RuntimeException("API Key was not set in secrets.properties")
        }

        Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)
        Places.createClient(this)
    }

    /**
     * Checks if the API key for Google Maps is properly configured in the application's metadata.
     *
     * This method retrieves the API key from the application's metadata, specifically looking for
     * a string value associated with the key "com.google.android.geo.maps3d.API_KEY".
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
                bundle.getString("com.google.android.geo.maps3d.API_KEY") // Key name is important!

            if (apiKey == null || apiKey.isBlank() || apiKey == "DEFAULT_API_KEY") {
                Toast.makeText(
                    this,
                    "API Key was not set in secrets.properties",
                    Toast.LENGTH_LONG
                ).show()
                throw RuntimeException("API Key was not set in secrets.properties")
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Package name not found.", e)
            throw RuntimeException("Error getting package info.", e)
        } catch (e: NullPointerException) {
            Log.e(TAG, "Error accessing meta-data.", e) // Handle the case where meta-data is completely missing.
            throw RuntimeException("Error accessing meta-data in manifest", e)
        }
    }
}

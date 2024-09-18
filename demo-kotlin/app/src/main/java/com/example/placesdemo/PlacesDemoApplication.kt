package com.example.placesdemo

import android.app.Application
import android.widget.Toast
import com.google.android.libraries.places.api.Places

class PlacesDemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val apiKey = BuildConfig.PLACES_API_KEY
        if (apiKey.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_api_key), Toast.LENGTH_LONG).show()
            return
        }

        Places.initialize(applicationContext, BuildConfig.PLACES_API_KEY)
    }
}
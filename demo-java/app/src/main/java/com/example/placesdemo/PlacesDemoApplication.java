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

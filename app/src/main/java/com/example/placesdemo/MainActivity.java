package com.example.placesdemo;

import com.google.android.libraries.places.api.Places;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    String apiKey = getString(R.string.places_api_key);

    if (apiKey.equals("")) {
      Toast.makeText(this, getString(R.string.error_api_key), Toast.LENGTH_LONG).show();
      return;
    }

    // Setup Places Client
    if (!Places.isInitialized()) {
      Places.initialize(apiKey);
    }

    setLaunchActivityClickListener(R.id.autocomplete_button, AutocompleteTestActivity.class);

    setLaunchActivityClickListener(R.id.place_and_photo_button, PlaceAndPhotoTestActivity.class);

    setLaunchActivityClickListener(R.id.current_place_button, CurrentPlaceTestActivity.class);
  }

  private void setLaunchActivityClickListener(
      int onClickResId, Class<? extends AppCompatActivity> activityClassToLaunch) {
    findViewById(onClickResId)
        .setOnClickListener(
            v -> startActivity(new Intent(MainActivity.this, activityClassToLaunch)));
  }
}

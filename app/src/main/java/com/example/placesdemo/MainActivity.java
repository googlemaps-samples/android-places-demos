package com.example.placesdemo;

import com.google.android.libraries.places.api.Places;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

  // TODO: Set API key.
  private String API_KEY = "AIzaasdf";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Setup Places Client
    if (!Places.isInitialized()) {
      Places.initialize(API_KEY);
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

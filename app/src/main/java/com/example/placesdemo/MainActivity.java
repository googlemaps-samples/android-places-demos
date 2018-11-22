/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.placesdemo;

import com.google.android.libraries.places.api.Places;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
      Places.initialize(getApplicationContext(), apiKey);
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

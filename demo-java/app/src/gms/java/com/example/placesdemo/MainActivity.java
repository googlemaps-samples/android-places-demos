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

import com.example.placesdemo.programmatic_autocomplete.ProgrammaticAutocompleteToolbarActivity;
import com.google.android.libraries.places.api.Places;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

  public static final String THEME_RES_ID_EXTRA = "widget_theme";

  private Spinner widgetThemeSpinner;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    final String apiKey = BuildConfig.PLACES_API_KEY;

    if (apiKey.equals("")) {
      Toast.makeText(this, getString(R.string.error_api_key), Toast.LENGTH_LONG).show();
      return;
    }

    // Setup Places Client
    if (!Places.isInitialized()) {
      Places.initialize(getApplicationContext(), apiKey);
    }

    setLaunchActivityClickListener(R.id.programmatic_autocomplete_button, ProgrammaticAutocompleteToolbarActivity.class);
    setLaunchActivityClickListener(R.id.autocomplete_button, AutocompleteTestActivity.class);
    setLaunchActivityClickListener(R.id.place_and_photo_button, PlaceAndPhotoTestActivity.class);
    setLaunchActivityClickListener(R.id.current_place_button, CurrentPlaceTestActivity.class);

    widgetThemeSpinner = findViewById(R.id.theme_spinner);
    widgetThemeSpinner.setAdapter(
            new ArrayAdapter<>(
                    /* context= */ this,
                    android.R.layout.simple_list_item_1,
                    Arrays.asList(
                            "Default", "\uD83D\uDCA9 brown", "\uD83E\uDD2E green", "\uD83D\uDE08 purple")));
  }

  private void setLaunchActivityClickListener(
      int onClickResId, Class<? extends AppCompatActivity> activityClassToLaunch) {
    findViewById(onClickResId)
        .setOnClickListener(
                v -> {
                  Intent intent = new Intent(MainActivity.this, activityClassToLaunch);
                  intent.putExtra(THEME_RES_ID_EXTRA, getSelectedTheme());
                  startActivity(intent);
                });
  }

  @StyleRes
  private int getSelectedTheme() {
    int style;
    switch (widgetThemeSpinner.getSelectedItemPosition()) {
      case 1:
        style = R.style.Brown;
        break;
      case 2:
        style = R.style.Green;
        break;
      case 3:
        style = R.style.Purple;
        break;
      default:
        style = 0;
    }
    return style;
  }
}

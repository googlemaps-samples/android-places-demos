/*
 * Copyright 2022 Google LLC
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

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.placesdemo.programmatic_autocomplete.ProgrammaticAutocompleteToolbarActivity;
import com.google.android.libraries.places.api.Places;

import androidx.activity.EdgeToEdge;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Enable edge-to-edge display. This must be called before calling super.onCreate().
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setLaunchActivityClickListener(R.id.autocomplete_button, PlaceAutocompleteActivity.class);
        setLaunchActivityClickListener(R.id.autocomplete_address_button, AutocompleteAddressActivity.class);
        setLaunchActivityClickListener(R.id.programmatic_autocomplete_button, ProgrammaticAutocompleteToolbarActivity.class
        );
        setLaunchActivityClickListener(R.id.place_and_photo_button, PlaceDetailsAndPhotosActivity.class);
        setLaunchActivityClickListener(R.id.is_open_button, PlaceIsOpenActivity.class);
        setLaunchActivityClickListener(R.id.current_place_button, CurrentPlaceActivity.class);
    }

    private void setLaunchActivityClickListener(
            int onClickResId, Class<? extends AppCompatActivity> activityClassToLaunch) {
        findViewById(onClickResId)
                .setOnClickListener(
                        v -> {
                            Intent intent = new Intent(MainActivity.this, activityClassToLaunch);
                            startActivity(intent);
                        });
    }
}

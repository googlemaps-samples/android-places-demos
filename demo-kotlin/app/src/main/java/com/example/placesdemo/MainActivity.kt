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
package com.example.placesdemo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import com.example.placesdemo.programmatic_autocomplete.ProgrammaticAutocompleteToolbarActivity
import com.google.android.libraries.places.api.Places

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val apiKey = BuildConfig.PLACES_API_KEY
        if (apiKey.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_api_key), Toast.LENGTH_LONG).show()
            return
        }

        // Setup Places Client
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }

        setLaunchActivityClickListener(R.id.autocomplete_button, PlaceAutocompleteActivity::class.java)
        setLaunchActivityClickListener(R.id.autocomplete_address_button, AutocompleteAddressActivity::class.java)
        setLaunchActivityClickListener(R.id.programmatic_autocomplete_button, ProgrammaticAutocompleteToolbarActivity::class.java)
        setLaunchActivityClickListener(R.id.current_place_button, CurrentPlaceActivity::class.java)
        setLaunchActivityClickListener(R.id.place_and_photo_button, PlaceDetailsAndPhotosActivity::class.java)
        setLaunchActivityClickListener(R.id.is_open_button, PlaceIsOpenActivity::class.java)
    }

    private fun setLaunchActivityClickListener(@IdRes onClickResId: Int, activityClassToLaunch: Class<out AppCompatActivity>) {
        findViewById<Button>(onClickResId).setOnClickListener {
            val intent = Intent(this@MainActivity, activityClassToLaunch)
            startActivity(intent)
        }
    }

}
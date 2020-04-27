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
package com.example.placesdemo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.example.placesdemo.programmatic_autocomplete.ProgrammaticAutocompleteToolbarActivity
import com.google.android.libraries.places.api.Places
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var widgetThemeSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val apiKey = getString(R.string.places_api_key)
        if (apiKey.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_api_key), Toast.LENGTH_LONG).show()
            return
        }

        // Setup Places Client
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }

        setLaunchActivityClickListener(R.id.programmatic_autocomplete_button, ProgrammaticAutocompleteToolbarActivity::class.java)
        setLaunchActivityClickListener(R.id.autocomplete_button, AutocompleteTestActivity::class.java)
        setLaunchActivityClickListener(R.id.place_and_photo_button, PlaceAndPhotoTestActivity::class.java)
        setLaunchActivityClickListener(R.id.current_place_button, CurrentPlaceTestActivity::class.java)

        widgetThemeSpinner = findViewById(R.id.theme_spinner)
        widgetThemeSpinner.adapter = ArrayAdapter( /* context= */
            this,
            android.R.layout.simple_list_item_1,
            listOf("Default", "\uD83D\uDCA9 brown", "\uD83E\uDD2E green", "\uD83D\uDE08 purple")
        )
    }

    private fun setLaunchActivityClickListener(@IdRes onClickResId: Int, activityClassToLaunch: Class<out AppCompatActivity>) {
        findViewById<Button>(onClickResId).setOnClickListener {
            val intent = Intent(this@MainActivity, activityClassToLaunch)
            intent.putExtra(THEME_RES_ID_EXTRA, selectedTheme)
            startActivity(intent)
        }
    }

    @get:StyleRes
    private val selectedTheme: Int
        get() {
            return when (widgetThemeSpinner.selectedItemPosition) {
                1 -> R.style.Brown
                2 -> R.style.Green
                3 -> R.style.Purple
                else -> 0
            }
        }

    companion object {
        const val THEME_RES_ID_EXTRA = "widget_theme"
    }
}
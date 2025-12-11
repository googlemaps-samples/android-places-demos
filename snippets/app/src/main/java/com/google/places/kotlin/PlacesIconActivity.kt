// Copyright 2023 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.places.kotlin

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.libraries.places.api.model.Place
import com.google.places.databinding.ActivityPlacesIconBinding

class PlacesIconActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlacesIconBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlacesIconBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "$title (Kotlin)"

        binding.placesIconButton.setOnClickListener {
            // In a real app, you would get a Place object from a Place Details request or similar.
            // For this snippet, we'll create a dummy Place object.
            val place = Place.builder()
                .setIconBackgroundColor(Color.BLUE)
                .setIconMaskUrl("https://maps.gstatic.com/mapfiles/place_api/icons/v1/png_71/generic_business-71.png")
                .build()
            getPlacesIcon(place)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getPlacesIcon(place: Place) {
        // [START maps_places_places_icon_and_bg_color]
        // Set the image view's background color to match the place's icon background color
        val bgColor = place.iconBackgroundColor ?: Color.TRANSPARENT
        binding.placesIconResult.setBackgroundColor(bgColor)

        // Fetch the icon using Glide and set the result in the image view
        Glide.with(this)
            .load(place.iconMaskUrl)
            .into(binding.placesIconResult)
        // [END maps_places_places_icon_and_bg_color]
    }
}
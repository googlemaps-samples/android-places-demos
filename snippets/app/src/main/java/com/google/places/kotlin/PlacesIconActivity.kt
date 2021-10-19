package com.google.places.kotlin

import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.libraries.places.api.model.Place
import com.bumptech.glide.Glide

class PlacesIconActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private fun getPlacesIcon(place: Place) {
        // [START maps_places_places_icon_and_bg_color]
        // Set the image view's background color to match the place's icon background color
        imageView.setBackgroundColor(place.iconBackgroundColor)

        // Fetch the icon using Glide and set the result in the image view
        Glide.with(this)
            .load(place.iconUrl)
            .into(imageView)
        // [END maps_places_places_icon_and_bg_color]
    }
}
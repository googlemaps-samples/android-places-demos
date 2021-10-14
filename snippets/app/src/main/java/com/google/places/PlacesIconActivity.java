package com.google.places;

import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.libraries.places.api.model.Place;

class PlacesIconActivity extends AppCompatActivity {

    private ImageView imageView;

    private void getPlacesIcon(Place place) {
        // [START maps_places_places_icon_and_bg_color]
        // Set the image view's background color to match the place's icon background color
        imageView.setBackgroundColor(place.getIconBackgroundColor());

        // Fetch the icon using Glide and set the result in the image view
        Glide.with(this)
            .load(place.getIconUrl())
            .into(imageView);
        // [END maps_places_places_icon_and_bg_color]
    }
}

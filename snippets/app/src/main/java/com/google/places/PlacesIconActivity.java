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

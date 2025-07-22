// Copyright 2025 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.placedetailscompose.ui.map

import android.content.res.Configuration
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
import com.example.placedetailscompose.R
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.PlaceDetailsCompactFragment
import com.google.android.libraries.places.widget.PlaceLoadListener
import com.google.android.libraries.places.widget.model.Orientation

@Composable
fun PlaceDetailsView(
    place: PointOfInterest,
    onDismiss: () -> Unit
) {
    val orientation =
        if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Orientation.HORIZONTAL
        } else {
            Orientation.VERTICAL
        }

        val fragment = remember(place) {
            PlaceDetailsCompactFragment.newInstance(
                PlaceDetailsCompactFragment.ALL_CONTENT,
                orientation,
            )
        }

    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context ->
            val view = LayoutInflater.from(context).inflate(R.layout.place_details_fragment, null) as FragmentContainerView
            view.id = R.id.fragment_container_view // Ensure a unique ID
            view
        },
        update = { view ->
            val fragmentManager = (view.context as? AppCompatActivity)?.supportFragmentManager
            if (fragmentManager != null) {
                fragmentManager.beginTransaction()
                    .replace(view.id, fragment)
                    .commit()

                fragment.setPlaceLoadListener(object : PlaceLoadListener {
                    override fun onSuccess(place: Place) {
                        Log.d("PlaceDetailsView", "Place loaded: $place")
                    }

                    override fun onFailure(e: Exception) {
                        Log.d("PlaceDetailsView", "Place failed to load place: ${e.message}")
                        onDismiss()
                    }
                })

                view.rootView.post {
                    fragment.loadWithPlaceId(place.placeId)
                }
            }
        }
    )
}

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

/**
 * This composable is responsible for displaying the place details fragment.
 * It uses the [AndroidView] composable to embed the [PlaceDetailsCompactFragment]
 * from the Places SDK into a Compose UI.
 *
 * @param place The point of interest to display details for.
 * @param onDismiss A callback to be invoked when the place details fragment is dismissed.
 */
@Composable
fun PlaceDetailsView(
    place: PointOfInterest,
    onDismiss: () -> Unit
) {
    // Determine the orientation of the device and set the orientation of the fragment accordingly.
    val orientation =
        if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Orientation.HORIZONTAL
        } else {
            Orientation.VERTICAL
        }

    // The `remember` composable is used to create and remember a `PlaceDetailsCompactFragment`
    // instance. The `key` is the `place` object, which means that a new fragment will be
    // created whenever the selected place changes. This is important because the fragment
    // is initialized with the place ID, and we need to create a new fragment to show a
    // different place.
    val fragment = remember(place) {
        PlaceDetailsCompactFragment.newInstance(
            PlaceDetailsCompactFragment.ALL_CONTENT,
            orientation,
        )
    }

    // The `AndroidView` composable is used to embed a classic Android View into a Compose UI.
    // It takes a `factory` lambda that is used to create the view, and an `update` lambda
    // that is used to update the view when the state changes.
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context ->
            // The `factory` lambda is called only once to create the view.
            // We inflate a `FragmentContainerView` from an XML layout file.
            // This view will be used to host the `PlaceDetailsCompactFragment`.
            val view = LayoutInflater.from(context).inflate(R.layout.place_details_fragment, null) as FragmentContainerView
            view.id = R.id.fragment_container_view // Ensure a unique ID
            view
        },
        update = { view ->
            // The `update` lambda is called whenever the state changes.
            // We get the `FragmentManager` from the context and use it to replace the
            // `FragmentContainerView` with the `PlaceDetailsCompactFragment`.
            val fragmentManager = (view.context as? AppCompatActivity)?.supportFragmentManager
            if (fragmentManager != null) {
                fragmentManager.beginTransaction()
                    .replace(view.id, fragment)
                    .commit()

                // We set a `PlaceLoadListener` on the fragment to be notified when the
                // place details are loaded.
                fragment.setPlaceLoadListener(object : PlaceLoadListener {
                    override fun onSuccess(place: Place) {
                        Log.d("PlaceDetailsView", "Place loaded: $place")
                    }

                    override fun onFailure(e: Exception) {
                        Log.d("PlaceDetailsView", "Place failed to load place: ${e.message}")
                        // If the place fails to load, we dismiss the fragment.
                        onDismiss()
                    }
                })

                // We need to post the `loadWithPlaceId` call to the view's message queue
                // to ensure that the fragment is attached to the view hierarchy before
                // the call is made.
                view.rootView.post {
                    fragment.loadWithPlaceId(place.placeId)
                }
            }
        }
    )
}

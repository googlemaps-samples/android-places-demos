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
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
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
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Determine the orientation of the device and set the orientation of the fragment accordingly.
    val orientation =
        if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Orientation.HORIZONTAL
        } else {
            Orientation.VERTICAL
        }

    val context = LocalContext.current
    val fragmentManager = (context as? AppCompatActivity)?.supportFragmentManager

    // Create a stable and unique ID for the FragmentContainerView
    val fragmentContainerId = remember { View.generateViewId() }

    // The `AndroidView` composable is used to embed a classic Android View into a Compose UI.
    // It takes a `factory` lambda that is used to create the view, and an `update` lambda
    // that is used to update the view when the state changes.
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            // The `factory` lambda is called only once to create the view.
            // We create a `FragmentContainerView` to host the `PlaceDetailsCompactFragment`.
            FragmentContainerView(context).apply {
                id = fragmentContainerId
            }
        },
        update = { view ->
            if (fragmentManager == null) return@AndroidView

            // The `update` lambda is called whenever the state changes.
            // We get the `FragmentManager` from the context and use it to manage the fragment.
            var fragment =
                fragmentManager.findFragmentById(view.id) as? PlaceDetailsCompactFragment

            if (fragment == null) {
                // If the fragment doesn't exist, create a new one and add it to the container.
                fragment = PlaceDetailsCompactFragment.newInstance(
                    PlaceDetailsCompactFragment.ALL_CONTENT,
                    orientation,
                )
                fragmentManager.beginTransaction()
                    .replace(view.id, fragment)
                    .commit()
            }

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
            view.post {
                fragment.loadWithPlaceId(place.placeId)
            }
        }
    )
}

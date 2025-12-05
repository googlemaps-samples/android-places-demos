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
 * This composable displays the **Compact** version of the Place Details UI.
 *
 * **Why use `AndroidView`?**
 * The Places UI Kit components (`PlaceDetailsCompactFragment` and `PlaceDetailsFragment`) are currently
 * implemented as Android Fragments, not native Composables. To use them in a Jetpack Compose app,
 * we need to bridge the gap using `AndroidView`. This allows us to host a legacy View (in this case,
 * a `FragmentContainerView`) inside our Compose layout.
 *
 * @param place The point of interest to display details for.
 * @param onDismiss A callback to be invoked when the place details fragment is dismissed.
 */
@Composable
fun PlaceDetailsCompactView(
    place: PointOfInterest,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // We need to know the device orientation to tell the Fragment how to lay itself out.
    // Although Compose handles layout differently, the underlying Fragment still relies on this signal.
    val orientation =
        if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Orientation.HORIZONTAL
        } else {
            Orientation.VERTICAL
        }

    val context = LocalContext.current
    // We need the FragmentManager to perform Fragment transactions (adding/removing the fragment).
    // We cast the Context to AppCompatActivity assuming this Composable is hosted within one.
    // In a production app, you might want a more robust way to provide the FragmentManager.
    val fragmentManager = (context as? AppCompatActivity)?.supportFragmentManager

    // **Why generate a View ID?**
    // The FragmentManager needs a unique ID to identify the container where the fragment will be placed.
    // `View.generateViewId()` gives us a safe, unique integer that won't collide with other views.
    // We use `remember` so this ID persists across recompositions.
    val fragmentContainerId = remember { View.generateViewId() }

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            // **The Factory Block**
            // This runs only once when the AndroidView is first created.
            // We create the container view that will hold our Fragment.
            FragmentContainerView(context).apply {
                id = fragmentContainerId
            }
        },
        update = { view ->
            // **The Update Block**
            // This runs whenever the Composable recomposes (e.g., when `place` changes).
            
            if (fragmentManager == null) return@AndroidView

            // Check if we've already added the fragment to this container.
            var fragment =
                fragmentManager.findFragmentById(view.id) as? PlaceDetailsCompactFragment

            if (fragment == null) {
                // **First Time Setup**
                // If the fragment doesn't exist yet, we create it and add it to the container.
                fragment = PlaceDetailsCompactFragment.newInstance(
                    PlaceDetailsCompactFragment.ALL_CONTENT,
                    orientation,
                )
                fragmentManager.beginTransaction()
                    .replace(view.id, fragment)
                    .commit()
            }

            // **Listening for Load Events**
            // We attach a listener to know when the data has successfully loaded or failed.
            // This allows us to react in the Compose layer (e.g., logging or dismissing on error).
            fragment.setPlaceLoadListener(object : PlaceLoadListener {
                override fun onSuccess(place: Place) {
                    Log.d("PlaceDetailsView", "Place loaded: $place")
                }

                override fun onFailure(e: Exception) {
                    Log.d("PlaceDetailsView", "Place failed to load place: ${e.message}")
                    onDismiss()
                }
            })

            // **Why `view.post`?**
            // This is a critical piece of glue code. The `loadWithPlaceId` method needs the Fragment's
            // view hierarchy to be fully initialized and attached to the window.
            // By posting this runnable to the view's message queue, we ensure that the load call
            // happens *after* the current layout pass is complete and the view is ready.
            // Without this, you might see crashes or undefined behavior.
            view.post {
                fragment.loadWithPlaceId(place.placeId)
            }
        }
    )
}

/**
 * This composable displays the **Full** version of the Place Details UI.
 *
 * It follows the same pattern as [PlaceDetailsCompactView], but wraps the [com.google.android.libraries.places.widget.PlaceDetailsFragment]
 * instead. This fragment takes up more screen space and shows more detailed information.
 */
@Composable
fun PlaceDetailsFullView(
    place: PointOfInterest,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val orientation =
        if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Orientation.HORIZONTAL
        } else {
            Orientation.VERTICAL
        }

    val context = LocalContext.current
    val fragmentManager = (context as? AppCompatActivity)?.supportFragmentManager

    val fragmentContainerId = remember { View.generateViewId() }

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            FragmentContainerView(context).apply {
                id = fragmentContainerId
            }
        },
        update = { view ->
            if (fragmentManager == null) return@AndroidView

            var fragment =
                fragmentManager.findFragmentById(view.id) as? com.google.android.libraries.places.widget.PlaceDetailsFragment

            if (fragment == null) {
                fragment = com.google.android.libraries.places.widget.PlaceDetailsFragment.newInstance(
                    com.google.android.libraries.places.widget.PlaceDetailsFragment.STANDARD_CONTENT,
                    orientation,
                )
                fragmentManager.beginTransaction()
                    .replace(view.id, fragment)
                    .commit()
            }

            fragment.setPlaceLoadListener(object : PlaceLoadListener {
                override fun onSuccess(place: Place) {
                    Log.d("PlaceDetailsFullView", "Place loaded: $place")
                }

                override fun onFailure(e: Exception) {
                    Log.d("PlaceDetailsFullView", "Place failed to load place: ${e.message}")
                    onDismiss()
                }
            })

            view.post {
                fragment.loadWithPlaceId(place.placeId)
            }
        }
    )
}

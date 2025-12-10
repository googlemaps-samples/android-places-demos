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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
import androidx.compose.ui.res.stringResource
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.PlaceDetailsCompactFragment
import com.google.android.libraries.places.widget.PlaceLoadListener
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
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
    place: Place,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: List<PlaceDetailsCompactFragment.Content> = PlaceDetailsCompactFragment.ALL_CONTENT,
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

    // **Why generate a View ID?**
    // The FragmentManager needs a unique ID to identify the container where the fragment will be placed.
    // `View.generateViewId()` gives us a safe, unique integer that won't collide with other views.
    // We use `remember` so this ID persists across recompositions.
    val fragmentContainerId = remember { View.generateViewId() }

    // We need the FragmentManager to perform Fragment transactions (adding/removing the fragment).
    // We cast the Context to AppCompatActivity assuming this Composable is hosted within one.
    // In a production app, you might want a more robust way to provide the FragmentManager.
    val fragmentManager = remember(context) {
        (context as? AppCompatActivity)?.supportFragmentManager
            ?: throw IllegalStateException("Context must be a FragmentActivity")
    }

    val fragment = remember(fragmentManager, fragmentContainerId, orientation, content) {
        fragmentManager.findFragmentById(fragmentContainerId) as? PlaceDetailsCompactFragment
            ?: PlaceDetailsCompactFragment.newInstance(
                content,
                orientation,
            ).also { fragment ->
                // **Listening for Load Events**
                fragment.setPlaceLoadListener(object : PlaceLoadListener {
                    override fun onSuccess(place: Place) {
                        Log.d("PlaceDetails", "Loading details for: ${place.id} at ${place.location}")
                    }

                    override fun onFailure(e: Exception) {
                        Log.d("PlaceDetailsView", "Place failed to load place: ${e.message}")
                        onDismiss()
                    }
                })
            }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                // **The Factory Block**
                // This runs only once when the AndroidView is first created.
                // We create the container view that will hold our Fragment.
                FragmentContainerView(context).apply {
                    id = fragmentContainerId
                    // Ensure the fragment is added.
                    // We use commit() (async) to allow the view to be attached before the transaction runs.
                    if (fragmentManager.findFragmentById(fragmentContainerId) == null) {
                        fragmentManager.beginTransaction()
                            .add(fragmentContainerId, fragment)
                            .commit()
                    }
                }
            },
            update = { view ->
                // **The Update Block**
                // This runs whenever the Composable recomposes (e.g., when `place` changes).

                // We post the update to ensure it runs after the fragment transaction has completed
                // and the fragment's view hierarchy is fully initialized.
                view.post {
                    // Load the place data
                    if (place.id != null) {
                        fragment.loadWithPlaceId(place.id!!)
                    } else if (place.location != null) {
                         fragment.loadWithCoordinates(place.location!!)
                    } else {
                        Log.e("PlaceDetailsView", "Place has no ID and no location: $place")
                    }
                }
            }
        )

        // Close Button
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(com.example.placedetailscompose.R.string.close),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * This composable displays the **Full** version of the Place Details UI.
 *
 * It follows the same pattern as [PlaceDetailsCompactView], but wraps the [com.google.android.libraries.places.widget.PlaceDetailsFragment]
 * instead. This fragment takes up more screen space and shows more detailed information.
 */
@Composable
fun PlaceDetailsFullView(
    place: Place,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: List<com.google.android.libraries.places.widget.PlaceDetailsFragment.Content> = com.google.android.libraries.places.widget.PlaceDetailsFragment.STANDARD_CONTENT,
) {
    val orientation =
        if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Orientation.HORIZONTAL
        } else {
            Orientation.VERTICAL
        }

    val context = LocalContext.current
    val fragmentManager = remember(context) {
        (context as? AppCompatActivity)?.supportFragmentManager
            ?: throw IllegalStateException("Context must be a FragmentActivity")
    }

    val fragmentContainerId = remember { View.generateViewId() }

    val fragment = remember(fragmentManager, fragmentContainerId, orientation, content) {
        fragmentManager.findFragmentById(fragmentContainerId) as? com.google.android.libraries.places.widget.PlaceDetailsFragment
            ?: com.google.android.libraries.places.widget.PlaceDetailsFragment.newInstance(
                content,
                orientation,
            ).also { fragment ->
                fragment.setPlaceLoadListener(object : PlaceLoadListener {
                    override fun onSuccess(place: Place) {
                        Log.d("PlaceDetailsFullView", "Place loaded: $place")
                    }

                    override fun onFailure(e: Exception) {
                        Log.d("PlaceDetailsFullView", "Place failed to load place: ${e.message}")
                        onDismiss()
                    }
                })
            }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Container for the bottom sheet content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { context ->
                    FragmentContainerView(context).apply {
                        id = fragmentContainerId
                        if (fragmentManager.findFragmentById(fragmentContainerId) == null) {
                            fragmentManager.beginTransaction()
                                .add(fragmentContainerId, fragment)
                                .commit()
                        }
                    }
                },
                update = { view ->
                    view.post {
                        if (place.id != null) {
                            fragment.loadWithPlaceId(place.id!!)
                        } else if (place.location != null) {
                            fragment.loadWithCoordinates(place.location!!)
                        } else {
                             Log.e("PlaceDetailsFullView", "Place has no ID and no location: $place")
                        }
                    }
                }
            )

            // Close Button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(com.example.placedetailscompose.R.string.close),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}


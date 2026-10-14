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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.compose.AdvancedPlaceDetailsCompact
import com.google.android.libraries.places.compose.AdvancedPlaceDetailsCompactContent
import com.google.android.libraries.places.compose.PlaceReference
import com.google.android.libraries.places.compose.action.MainAction
import com.google.android.libraries.places.widget.PlaceDetailsFragment
import com.google.android.libraries.places.widget.PlaceLoadListener
import com.google.android.libraries.places.widget.model.Orientation
import com.google.android.libraries.places.compose.Orientation as ComposeOrientation

/**
 * This composable displays the **Compact** version of the Place Details UI using the native
 * Jetpack Compose [AdvancedPlaceDetailsCompact] component introduced in Places SDK for Android 6.0.2.
 *
 * Unlike `PlaceDetailsFragment` (which is a Fragment hosted via `AndroidView`),
 * [AdvancedPlaceDetailsCompact] is a first-class Jetpack Compose component that accepts a
 * [PlaceReference] (`PlaceId`, `Coordinates`, or `ResourceName`), configurable content sections,
 * main/corner actions, and custom composable content slots (`basicSectionTop` and
 * `basicSectionBottom`).
 *
 * @param place The point of interest to display details for.
 * @param onDismiss A callback to be invoked when the place details card is dismissed.
 * @param modifier Optional [Modifier] for the container.
 * @param content Set of [AdvancedPlaceDetailsCompactContent] sections to render.
 * @param showCustomSlots Whether to render custom composable content in `basicSectionTop` and `basicSectionBottom`.
 */
@Composable
fun PlaceDetailsCompactView(
    place: Place,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: Set<AdvancedPlaceDetailsCompactContent> = AdvancedPlaceDetailsCompactContent.ALL_CONTENT,
    showCustomSlots: Boolean = true,
) {
    val orientation =
        if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ComposeOrientation.HORIZONTAL
        } else {
            ComposeOrientation.VERTICAL
        }

    val reference: PlaceReference? = remember(place.id, place.location) {
        when {
            place.id != null -> PlaceReference.PlaceId(place.id!!)

            place.location != null -> PlaceReference.Coordinates(
                place.location!!.latitude,
                place.location!!.longitude,
            )

            else -> null
        }
    }

    if (reference == null) {
        Log.e("PlaceDetailsView", "Place has no ID and no location: $place")
        return
    }

    Box(modifier = modifier.fillMaxWidth()) {
        AdvancedPlaceDetailsCompact(
            reference = reference,
            modifier = Modifier.fillMaxWidth(),
            orientation = orientation,
            content = content,
            mainActions = {
                listOf(
                    MainAction.OpenDirections(),
                    MainAction.OpenWebsite(),
                    MainAction.Call(),
                    MainAction.OpenInMaps(),
                )
            },
            onPlaceLoaded = { loadedPlace ->
                Log.d("PlaceDetails", "Loaded details for: ${loadedPlace.id} at ${loadedPlace.location}")
            },
            onPlaceLoadFailed = { exception ->
                Log.d("PlaceDetailsView", "Failed to load place: ${exception.message}")
                onDismiss()
            },
        ) {
            if (showCustomSlots) {
                basicSectionTop {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(bottom = 4.dp),
                    ) {
                        Text(
                            text = "Featured Place",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                }
                basicSectionBottom {
                    val summary = this.place.editorialSummary
                    if (!summary.isNullOrBlank()) {
                        Text(
                            text = summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }

        // Close Button
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), shape = CircleShape),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(com.example.placedetailscompose.R.string.close),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/**
 * This composable displays the **Full** version of the Place Details UI.
 *
 * It follows the same pattern as [PlaceDetailsCompactView], but wraps the [PlaceDetailsFragment]
 * instead. This fragment takes up more screen space and shows more detailed information.
 */
@Composable
fun PlaceDetailsFullView(
    place: Place,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: List<PlaceDetailsFragment.Content> = PlaceDetailsFragment.STANDARD_CONTENT,
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
        fragmentManager.findFragmentById(fragmentContainerId) as? PlaceDetailsFragment
            ?: PlaceDetailsFragment.newInstance(
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
                .align(Alignment.BottomCenter),
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
                },
            )

            // Close Button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), shape = CircleShape),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(com.example.placedetailscompose.R.string.close),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.libraries.places.samples.kotlincompose.screens

import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.libraries.places.samples.kotlincompose.core.Demo
import com.google.android.libraries.places.samples.kotlincompose.core.DemoCategory
import com.google.android.libraries.places.widget.AdvancedPlaceDetailsCompactFragment
import com.google.android.libraries.places.widget.AdvancedPlaceDetailsFragment

@Demo(
    title = "Places UI Kit & Actions",
    description = "AdvancedPlaceDetailsFragment, AdvancedPlaceDetailsCompactFragment, and Action Controllers.",
    category = DemoCategory.PLACES_UI_KIT_AND_ACTIONS,
    route = "uikit_actions"
)
class PlacesUIKitAndActionsDemoScreen {
    @Composable
    fun Content() {
        val context = LocalContext.current
        var placeId by remember { mutableStateOf("ChIJ3S-JXYerEmsRUcioZayKtNo") } // Sydney Opera House
        var selectedTabIndex by remember { mutableIntStateOf(0) }
        var actionLog by remember { mutableStateOf("Action logs will appear here when user interacts with UI Kit components.") }

        val tabs = listOf("Full Fragment", "Compact Fragment")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = placeId,
                onValueChange = { placeId = it },
                label = { Text("Target Place ID") },
                modifier = Modifier.fillMaxWidth()
            )

            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "Action Controller Log", style = MaterialTheme.typography.titleSmall)
                    Text(text = actionLog, style = MaterialTheme.typography.bodySmall)
                }
            }

            val fragmentActivity = context as? FragmentActivity
            if (fragmentActivity != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    val containerId = remember { View.generateViewId() }

                    AndroidView(
                        factory = { ctx ->
                            FrameLayout(ctx).apply {
                                id = containerId
                            }
                        },
                        update = { frameLayout ->
                            val fragmentManager = fragmentActivity.supportFragmentManager
                            val tag = "placedetails_fragment_$selectedTabIndex"
                            
                            val existingFragment = fragmentManager.findFragmentByTag(tag)
                            if (existingFragment == null) {
                                val fragment = if (selectedTabIndex == 0) {
                                    AdvancedPlaceDetailsFragment.newInstance(
                                        AdvancedPlaceDetailsFragment.ALL_CONTENT
                                    ).apply {
                                        loadWithPlaceId(placeId)
                                    }
                                } else {
                                    AdvancedPlaceDetailsCompactFragment.newInstance(
                                        AdvancedPlaceDetailsCompactFragment.ALL_CONTENT
                                    ).apply {
                                        loadWithPlaceId(placeId)
                                    }
                                }

                                fragmentManager.beginTransaction()
                                    .replace(frameLayout.id, fragment, tag)
                                    .commitAllowingStateLoss()
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Text(
                    text = "FragmentHost requires a FragmentActivity context.",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

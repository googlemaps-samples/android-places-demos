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

package com.google.android.libraries.places.samples.kotlincompose.core

import com.google.android.libraries.places.samples.kotlincompose.screens.CoreInitializationDemoScreen
import com.google.android.libraries.places.samples.kotlincompose.screens.PlaceAttributesAndHoursDemoScreen
import com.google.android.libraries.places.samples.kotlincompose.screens.PlaceDetailsAndPhotosDemoScreen
import com.google.android.libraries.places.samples.kotlincompose.screens.PlacesUIKitAndActionsDemoScreen
import com.google.android.libraries.places.samples.kotlincompose.screens.SearchAndDiscoveryDemoScreen

data class DemoItem(
    val title: String,
    val description: String,
    val category: DemoCategory,
    val route: String,
    val screenClass: Class<*>
)

object DemoRegistry {
    val registeredClasses: List<Class<*>> = listOf(
        CoreInitializationDemoScreen::class.java,
        SearchAndDiscoveryDemoScreen::class.java,
        PlaceDetailsAndPhotosDemoScreen::class.java,
        PlaceAttributesAndHoursDemoScreen::class.java,
        PlacesUIKitAndActionsDemoScreen::class.java
    )

    val demos: List<DemoItem> by lazy {
        registeredClasses.mapNotNull { clazz ->
            val annotation = clazz.getAnnotation(Demo::class.java)
            annotation?.let {
                DemoItem(
                    title = it.title,
                    description = it.description,
                    category = it.category,
                    route = it.route,
                    screenClass = clazz
                )
            }
        }
    }

    fun searchDemos(query: String): List<DemoItem> {
        if (query.isBlank()) return demos
        return demos.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true) ||
                    it.category.title.contains(query, ignoreCase = true)
        }
    }

    fun getDemosByCategory(category: DemoCategory): List<DemoItem> {
        return demos.filter { it.category == category }
    }
}

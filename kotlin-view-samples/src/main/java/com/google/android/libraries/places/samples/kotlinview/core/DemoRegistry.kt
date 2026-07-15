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

package com.google.android.libraries.places.samples.kotlinview.core

import android.app.Activity
import com.google.android.libraries.places.samples.kotlinview.CoreInitializationDemoActivity
import com.google.android.libraries.places.samples.kotlinview.PlaceAttributesAndHoursDemoActivity
import com.google.android.libraries.places.samples.kotlinview.PlaceDetailsAndPhotosDemoActivity
import com.google.android.libraries.places.samples.kotlinview.PlacesUIKitAndActionsDemoActivity
import com.google.android.libraries.places.samples.kotlinview.SearchAndDiscoveryDemoActivity

data class DemoItem(
    val title: String,
    val description: String,
    val category: DemoCategory,
    val activityClass: Class<out Activity>,
    val order: Int
)

object DemoRegistry {
    val DEMO_ACTIVITIES: List<Class<out Activity>> = listOf(
        CoreInitializationDemoActivity::class.java,
        SearchAndDiscoveryDemoActivity::class.java,
        PlaceDetailsAndPhotosDemoActivity::class.java,
        PlaceAttributesAndHoursDemoActivity::class.java,
        PlacesUIKitAndActionsDemoActivity::class.java
    )

    fun getDemos(): List<DemoItem> {
        return DEMO_ACTIVITIES.mapNotNull { activityClass ->
            val annotation = activityClass.getAnnotation(Demo::class.java)
            if (annotation != null) {
                DemoItem(
                    title = annotation.title,
                    description = annotation.description,
                    category = annotation.category,
                    activityClass = activityClass,
                    order = annotation.order
                )
            } else null
        }.sortedWith(compareBy({ it.category.ordinal }, { it.order }))
    }

    fun searchDemos(query: String): List<DemoItem> {
        val allDemos = getDemos()
        if (query.isBlank()) return allDemos
        val lower = query.lowercase().trim()
        return allDemos.filter { demo ->
            demo.title.lowercase().contains(lower) ||
                    demo.description.lowercase().contains(lower) ||
                    demo.category.title.lowercase().contains(lower)
        }
    }
}

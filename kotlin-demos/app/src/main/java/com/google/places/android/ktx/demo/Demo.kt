// Copyright 2020 Google LLC
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

package com.google.places.android.ktx.demo

import android.app.Activity
import androidx.annotation.StringRes

enum class Demo(
    @StringRes val title: Int,
    @StringRes val description: Int,
    val activity: Class<out Activity>
) {
    AUTOCOMPLETE_FRAGMENT_DEMO(
        R.string.autocomplete_fragment_demo_title,
        R.string.autocomplete_fragment_demo_description,
        AutocompleteDemoActivity::class.java
    ),
    PLACES_SEARCH_DEMO(
        R.string.places_demo_title,
        R.string.places_demo_description,
        PlacesSearchDemoActivity::class.java
    ),
    PLACES_PHOTO_DEMO(
        R.string.places_photo_demo_title,
        R.string.places_photo_demo_description,
        PlacesPhotoDemoActivity::class.java
    )
}
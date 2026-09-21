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

enum class DemoCategory(val title: String, val description: String) {
    CORE_INITIALIZATION(
        "Core & App Check",
        "Initialization, App Check status, and deinitialization"
    ),
    SEARCH_AND_DISCOVERY(
        "Search & Discovery",
        "Text Search, Nearby Search, Find Current Place, Autocomplete"
    ),
    PLACE_DETAILS_AND_PHOTOS(
        "Place Details & Photos",
        "Fetch Place, Fetch Photo, Fetch Resolved Photo URI, Address Descriptor"
    ),
    PLACE_ATTRIBUTES_AND_HOURS(
        "Place Attributes & Hours",
        "IsOpen status, opening hours, sub-destinations, EV/accessibility"
    ),
    PLACES_UI_KIT_AND_ACTIONS(
        "UI Kit & Actions",
        "Advanced Place Details Fragments & Place Action Provider"
    )
}

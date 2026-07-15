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

enum class DemoCategory(val title: String, val description: String) {
    INITIALIZATION(
        "Initialization & App Check",
        "Places SDK initialization, App Check status, and client options"
    ),
    SEARCH_AND_DISCOVERY(
        "Search & Discovery",
        "searchByText, searchNearby, findCurrentPlace, and Autocomplete"
    ),
    DETAILS_AND_PHOTOS(
        "Place Details & Photos",
        "fetchPlace, fetchPhoto, fetchResolvedPhotoUri, and AddressDescriptor"
    ),
    ATTRIBUTES_AND_HOURS(
        "Attributes & Hours",
        "isOpen, price range, containing place, EV options, and accessibility"
    ),
    UI_KIT_AND_ACTIONS(
        "UI Kit & Actions",
        "AdvancedPlaceDetailsFragment, AdvancedPlaceDetailsCompactFragment, and PlaceActionProvider"
    )
}

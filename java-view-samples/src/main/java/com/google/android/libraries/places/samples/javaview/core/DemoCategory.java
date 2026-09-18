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

package com.google.android.libraries.places.samples.javaview.core;

import androidx.annotation.StringRes;
import com.google.android.libraries.places.samples.javaview.R;

/** Categorized grouping for Places SDK Java View demos. */
public enum DemoCategory {
    CORE_INITIALIZATION(R.string.category_core_init, R.string.category_core_init_desc),
    SEARCH_DISCOVERY(R.string.category_search_discovery, R.string.category_search_discovery_desc),
    PLACE_DETAILS_PHOTOS(R.string.category_details_photos, R.string.category_details_photos_desc),
    PLACE_ATTRIBUTES_HOURS(R.string.category_attributes_hours, R.string.category_attributes_hours_desc),
    PLACES_UI_KIT_ACTIONS(R.string.category_uikit_actions, R.string.category_uikit_actions_desc);

    @StringRes private final int titleResId;
    @StringRes private final int descriptionResId;

    DemoCategory(@StringRes int titleResId, @StringRes int descriptionResId) {
        this.titleResId = titleResId;
        this.descriptionResId = descriptionResId;
    }

    public int getTitleResId() {
        return titleResId;
    }

    public int getDescriptionResId() {
        return descriptionResId;
    }
}

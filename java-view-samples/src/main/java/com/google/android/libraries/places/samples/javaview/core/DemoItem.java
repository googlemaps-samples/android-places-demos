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

import android.app.Activity;
import android.content.Context;

/** Data model representing a registered demo item extracted from @Demo metadata. */
public class DemoItem {
    private final Class<? extends Activity> activityClass;
    private final DemoCategory category;
    private final String title;
    private final String description;

    public DemoItem(Class<? extends Activity> activityClass, DemoCategory category, String title, String description) {
        this.activityClass = activityClass;
        this.category = category;
        this.title = title;
        this.description = description;
    }

    public Class<? extends Activity> getActivityClass() {
        return activityClass;
    }

    public DemoCategory getCategory() {
        return category;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean matches(Context context, String query, DemoCategory filterCategory) {
        if (filterCategory != null && category != filterCategory) {
            return false;
        }
        if (query == null || query.trim().isEmpty()) {
            return true;
        }
        String lowerQuery = query.trim().toLowerCase();
        String titleStr = title.toLowerCase();
        String descStr = description.toLowerCase();
        return titleStr.contains(lowerQuery) || descStr.contains(lowerQuery);
    }
}

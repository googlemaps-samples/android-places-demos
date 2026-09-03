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
import com.google.android.libraries.places.samples.javaview.demos.CoreInitializationDemoActivity;
import com.google.android.libraries.places.samples.javaview.demos.PlaceAttributesAndHoursDemoActivity;
import com.google.android.libraries.places.samples.javaview.demos.PlaceDetailsAndPhotosDemoActivity;
import com.google.android.libraries.places.samples.javaview.demos.PlacesUIKitAndActionsDemoActivity;
import com.google.android.libraries.places.samples.javaview.demos.SearchAndDiscoveryDemoActivity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Registry engine that inspects @Demo annotations on registered Activity classes. */
public class DemoRegistry {

    private static final List<Class<? extends Activity>> DEMO_CLASSES = Collections.unmodifiableList(
        Arrays.asList(
            CoreInitializationDemoActivity.class,
            SearchAndDiscoveryDemoActivity.class,
            PlaceDetailsAndPhotosDemoActivity.class,
            PlaceAttributesAndHoursDemoActivity.class,
            PlacesUIKitAndActionsDemoActivity.class
        )
    );

    private static List<DemoItem> cachedItems = null;

    public static synchronized List<DemoItem> getDemos() {
        if (cachedItems != null) {
            return cachedItems;
        }

        List<DemoItem> items = new ArrayList<>();
        for (Class<? extends Activity> clazz : DEMO_CLASSES) {
            Demo annotation = clazz.getAnnotation(Demo.class);
            if (annotation != null) {
                items.add(new DemoItem(clazz, annotation.category(), annotation.title(), annotation.description()));
            }
        }
        cachedItems = Collections.unmodifiableList(items);
        return cachedItems;
    }

    public static List<DemoItem> filterDemos(Context context, String query, DemoCategory category) {
        List<DemoItem> filtered = new ArrayList<>();
        for (DemoItem item : getDemos()) {
            if (item.matches(context, query, category)) {
                filtered.add(item);
            }
        }
        return filtered;
    }
}

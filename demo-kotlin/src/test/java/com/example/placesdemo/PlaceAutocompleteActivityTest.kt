/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.placesdemo

import android.content.Intent
import android.widget.TextView
import androidx.activity.result.ActivityResultRegistry
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.widget.PlaceAutocompleteActivity as PlacesSdkAutocompleteActivity
import com.google.common.truth.Truth.assertThat
import java.lang.reflect.Field
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Unit test verifying error handling for [PlaceAutocompleteActivity].
 *
 * ## Background & Context (Issue #733)
 * When launching the Places Autocomplete widget activity via `registerForActivityResult`,
 * failures (such as missing/invalid API keys or network errors) result in a result code of
 * [PlacesSdkAutocompleteActivity.RESULT_ERROR]. The Places SDK bundles a [Status] object
 * containing human-readable error details under the `"places/status"` Intent extra of the
 * returned result Intent (`result.data`).
 *
 * Previously, the callback in [PlaceAutocompleteActivity] incorrectly referenced `intent`
 * (which resolved to `this.intent`, the Activity's launch intent) rather than `result.data`.
 * Consequently, the error status was never parsed and the error message remained empty.
 *
 * This test uses Robolectric to launch [PlaceAutocompleteActivity], simulates an error result
 * delivered from the Places Autocomplete widget, and verifies that the error message is
 * correctly extracted from `result.data` and displayed in the UI.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class PlaceAutocompleteActivityTest {

    @Before
    fun setUp() {
        // Places SDK requires initialization before any activity using it is launched.
        if (!Places.isInitialized()) {
            Places.initialize(ApplicationProvider.getApplicationContext(), "fake_api_key")
        }
    }

    @Test
    fun autocompleteResultError_updatesResponseWithStatusMessage() {
        // --- 1. Prepare Simulated Error Data ---
        // Places SDK returns RESULT_ERROR with a Status object inside the result intent extras.
        val errorMessage = "API key is invalid or unauthorized"
        val errorStatus = Status(PlacesSdkAutocompleteActivity.RESULT_ERROR, errorMessage)
        val resultData = Intent().apply {
            putExtra("places/status", errorStatus)
        }

        // --- 2. Launch Activity Under Test ---
        val scenario = ActivityScenario.launch(PlaceAutocompleteActivity::class.java)

        scenario.onActivity { activity ->
            val registry = activity.activityResultRegistry

            // --- 3. Locate Launcher in ActivityResultRegistry ---
            // Find the request code and key assigned to the autocomplete launcher.
            val (requestCode, key) = getAutocompleteRequestCodeAndKey(registry)

            // --- 4. Mark Key as Launched ---
            // ActivityResultRegistry requires the key to be in the launched list before it will dispatch
            // the result to the registered callback.
            markKeyAsLaunched(registry, key)

            // --- 5. Dispatch Simulated Error Result ---
            // This invokes the callback registered with registerForActivityResult in PlaceAutocompleteActivity.
            registry.dispatchResult(
                requestCode,
                PlacesSdkAutocompleteActivity.RESULT_ERROR,
                resultData
            )

            // --- 6. Verify UI State ---
            // PlaceAutocompleteActivity should extract statusMessage from result.data and display it.
            val responseTextView = activity.findViewById<TextView>(R.id.response)
            assertThat(responseTextView.text.toString()).isEqualTo(errorMessage)
        }
    }

    /**
     * Resolves the request code and key for the activity launcher registered by [PlaceAutocompleteActivity].
     *
     * ComponentActivity registers activity result contracts with keys formatted as `activity_rq#<index>`.
     * This helper locates the request code corresponding to the autocomplete activity launcher in the registry.
     */
    @Suppress("UNCHECKED_CAST")
    private fun getAutocompleteRequestCodeAndKey(registry: ActivityResultRegistry): Pair<Int, String> {
        val rcToKeyField = findField(registry.javaClass, "mRcToKey", "rcToKey")
        val rcToKey = rcToKeyField.get(registry) as Map<Int, String>
        val entry = rcToKey.entries.firstOrNull { it.value.startsWith("activity_rq#") }
            ?: error("No activity result launcher key found starting with 'activity_rq#'. Available: ${rcToKey.values}")
        return entry.key to entry.value
    }

    /**
     * Marks an activity result request key as "launched" in the [ActivityResultRegistry].
     *
     * When [ActivityResultRegistry.dispatchResult] is called, AndroidX verifies that the key
     * is present in its internal `launchedKeys` tracking list before routing the result to the
     * registered callback. In a unit test simulating a result without actually executing the
     * foreign target activity, we mark the key as launched so the registry delivers the result.
     */
    @Suppress("UNCHECKED_CAST")
    private fun markKeyAsLaunched(registry: ActivityResultRegistry, key: String) {
        val field = findField(registry.javaClass, "mLaunchedKeys", "launchedKeys")
        val launchedKeys = field.get(registry) as MutableList<String>
        if (!launchedKeys.contains(key)) {
            launchedKeys.add(key)
        }
    }

    /**
     * Traverses the class hierarchy of [targetClass] to find a declared field matching any of the given [names].
     *
     * AndroidX internal implementations often vary field names between versions (e.g., prefixing with 'm'
     * or renaming across Kotlin/Java migrations). This helper allows robust reflection lookup across versions.
     */
    private fun findField(targetClass: Class<*>, vararg names: String): Field {
        var current: Class<*>? = targetClass
        while (current != null) {
            for (field in current.declaredFields) {
                if (names.any { it.equals(field.name, ignoreCase = true) }) {
                    field.isAccessible = true
                    return field
                }
            }
            current = current.superclass
        }
        error("Could not find any field matching ${names.toList()} in class hierarchy of $targetClass")
    }
}

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
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.widget.PlaceAutocompleteActivity as PlacesSdkAutocompleteActivity
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class PlaceAutocompleteActivityTest {

    @Before
    fun setUp() {
        if (!Places.isInitialized()) {
            Places.initialize(ApplicationProvider.getApplicationContext(), "fake_api_key")
        }
    }

    @Test
    fun autocompleteResultError_updatesResponseWithStatusMessage() {
        val scenario = ActivityScenario.launch(PlaceAutocompleteActivity::class.java)

        val errorMessage = "API key is invalid or unauthorized"
        val errorStatus = Status(PlacesSdkAutocompleteActivity.RESULT_ERROR, errorMessage)
        val resultData = Intent().apply {
            putExtra("places/status", errorStatus)
        }

        scenario.onActivity { activity ->
            // Locate registered request code in ActivityResultRegistry
            val registry = activity.activityResultRegistry
            var rcToKeyField: java.lang.reflect.Field? = null
            var curr: Class<*>? = registry.javaClass
            while (curr != null) {
                for (f in curr.declaredFields) {
                    if (f.name == "rcToKey" || f.name == "mRcToKey" || f.name.contains("rcToKey", ignoreCase = true)) {
                        rcToKeyField = f
                        break
                    }
                }
                if (rcToKeyField != null) break
                curr = curr.superclass
            }
            if (rcToKeyField == null) {
                error("Could not find rcToKey field. Available fields: " +
                    generateSequence<Class<*>>(registry.javaClass) { it.superclass }
                        .flatMap { it.declaredFields.asSequence() }
                        .map { it.name }
                        .toList()
                )
            }
            rcToKeyField.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val rcToKey = rcToKeyField.get(registry) as Map<Int, String>
            val entry = rcToKey.entries.first { it.value.startsWith("activity_rq#") }
            val requestCode = entry.key
            val key = entry.value

            var launchedKeysField: java.lang.reflect.Field? = null
            var currL: Class<*>? = registry.javaClass
            while (currL != null) {
                for (f in currL.declaredFields) {
                    if (f.name == "launchedKeys" || f.name == "mLaunchedKeys") {
                        launchedKeysField = f
                        break
                    }
                }
                if (launchedKeysField != null) break
                currL = currL.superclass
            }
            launchedKeysField?.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            (launchedKeysField?.get(registry) as? MutableList<String>)?.add(key)

            registry.dispatchResult(
                requestCode,
                PlacesSdkAutocompleteActivity.RESULT_ERROR,
                resultData
            )

            val responseTextView = activity.findViewById<TextView>(R.id.response)
            assertThat(responseTextView.text.toString()).isEqualTo(errorMessage)
        }
    }
}

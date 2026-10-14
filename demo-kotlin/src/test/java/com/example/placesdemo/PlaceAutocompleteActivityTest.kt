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
import androidx.activity.result.ActivityResult
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import com.google.android.libraries.places.widget.PlaceAutocompleteActivity as PlacesSdkAutocompleteActivity

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
 * This test uses Robolectric to launch [PlaceAutocompleteActivity], invokes the
 * `@VisibleForTesting`-annotated result handlers with a simulated [ActivityResult], and
 * verifies that the error message is extracted from `result.data` and displayed in the UI
 * without relying on reflection.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class PlaceAutocompleteActivityTest {

    @Before
    fun setUp() {
        // Places SDK requires initialization before any activity using it is launched.
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(
                ApplicationProvider.getApplicationContext(),
                "fake_api_key",
            )
        }
    }

    @Test
    fun autocompleteResultError_updatesResponseWithStatusMessage() {
        val errorMessage = "API key is invalid or unauthorized"
        val errorStatus = Status(PlacesSdkAutocompleteActivity.RESULT_ERROR, errorMessage)
        val resultData = Intent().apply {
            putExtra("places/status", errorStatus)
        }

        ActivityScenario.launch(PlaceAutocompleteActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.handleAutocompleteResult(
                    ActivityResult(PlacesSdkAutocompleteActivity.RESULT_ERROR, resultData),
                )

                val responseTextView = activity.findViewById<TextView>(R.id.response)
                assertThat(responseTextView.text.toString()).isEqualTo(errorMessage)
            }
        }
    }

    @Test
    fun basicAutocompleteResultError_updatesResponseWithStatusMessage() {
        val errorMessage = "Quota exceeded for BasicPlaceAutocomplete"
        val errorStatus = Status(PlacesSdkAutocompleteActivity.RESULT_ERROR, errorMessage)
        val resultData = Intent().apply {
            putExtra("places/status", errorStatus)
        }

        ActivityScenario.launch(PlaceAutocompleteActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.handleBasicAutocompleteResult(
                    ActivityResult(PlacesSdkAutocompleteActivity.RESULT_ERROR, resultData),
                )

                val responseTextView = activity.findViewById<TextView>(R.id.response)
                assertThat(responseTextView.text.toString()).isEqualTo(errorMessage)
            }
        }
    }
}

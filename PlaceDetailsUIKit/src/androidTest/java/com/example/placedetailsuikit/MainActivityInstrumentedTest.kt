// Copyright 2025 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.example.placedetailsuikit

import android.Manifest
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [MainActivity] to verify UI behavior and state management.
 * These tests run on an Android device or emulator.
 */
@RunWith(AndroidJUnit4::class)
class MainActivityInstrumentedTest {

    // A Rule to launch MainActivity before each test and clean it up afterward.
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    // A Rule to grant location permissions before each test. This prevents the permission dialog
    // from interrupting the test flow.
    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    /**
     * Test to verify that the map container is displayed when the activity starts.
     */
    @Test
    fun test_mapIsDisplayedOnLaunch() {
        onView(withId(R.id.map_fragment)).check(matches(isDisplayed()))
    }

    /**
     * Test the full user flow:
     * 1. Simulate a POI click on the map.
     * 2. Verify the Place Details UI appears (with a loader first, then the content).
     * 3. Click the dismiss button.
     * 4. Verify the Place Details UI is hidden.
     */
    @Test
    fun test_poiClickAndDismissFlow() {
        // --- 1. Simulate a POI Click ---
        // We get the activity scenario and trigger onPoiClick directly on the UI thread.
        // This is a reliable way to test the UI logic without actually tapping the map.
        activityRule.scenario.onActivity { activity ->
            // A mock POI for "Google Sydney"
            val poi = PointOfInterest(
                LatLng(-33.865072, 151.1961474),
                "ChIJP3Sa8ziYEmsRUKgyFmh9AQM",
                "Google Sydney"
            )
            activity.onPoiClick(poi)
        }

        // --- 2. Verify UI After Click ---
        // The wrapper view containing the fragment should now be visible.
        onView(withId(R.id.place_details_wrapper)).check(matches(isDisplayed()))

        // Check for the loading indicator that is a child of our wrapper.
        // This avoids ambiguity with the loader inside the PlaceDetailsCompactFragment.
        onView(
            allOf(
                withId(R.id.loading_indicator_main),
                withParent(withId(R.id.place_details_wrapper))
            )
        ).check(matches(isDisplayed()))


        // The Place Details fragment loads data asynchronously. For a simple sample,
        // a short sleep is a straightforward way to wait for the UI to update.
        // For a production app, using Espresso Idling Resources is the recommended approach.
        Thread.sleep(3000) // Wait for 3 seconds for the network call to complete.

        // Check that our specific loader is now gone.
        onView(
            allOf(
                withId(R.id.loading_indicator_main),
                withParent(withId(R.id.place_details_wrapper))
            )
        ).check(matches(not(isDisplayed())))

        onView(withId(R.id.place_details_container)).check(matches(isDisplayed()))
        onView(withId(R.id.dismiss_button)).check(matches(isDisplayed()))

        // --- 3. Click the Dismiss Button ---
        onView(withId(R.id.dismiss_button)).perform(click())

        // --- 4. Verify UI After Dismiss ---
        // The wrapper view should now be hidden.
        onView(withId(R.id.place_details_wrapper)).check(matches(not(isDisplayed())))
    }

    /**
     * Test that the Place Details view's state is correctly restored after a configuration change
     * (e.g., screen rotation), thanks to the ViewModel.
     */
    @Test
    fun test_stateRestoresOnConfigurationChange() {
        // --- 1. Show the Place Details Fragment ---
        activityRule.scenario.onActivity { activity ->
            val poi = PointOfInterest(
                LatLng(-33.865072, 151.1961474),
                "ChIJP3Sa8ziYEmsRUKgyFmh9AQM",
                "Google Sydney"
            )
            activity.onPoiClick(poi)
        }

        // Wait for it to load.
        Thread.sleep(3000)
        onView(withId(R.id.place_details_wrapper)).check(matches(isDisplayed()))

        // --- 2. Recreate the Activity (Simulates Rotation) ---
        activityRule.scenario.recreate()

        // --- 3. Verify the UI is still visible ---
        // Add another wait after recreation for the fragment to reload and become visible.
        Thread.sleep(3000)

        // The wrapper should still be visible without needing another click because the
        // selected place ID was restored from the ViewModel.
        onView(withId(R.id.place_details_wrapper)).check(matches(isDisplayed()))
        onView(withId(R.id.place_details_container)).check(matches(isDisplayed()))
        onView(withId(R.id.dismiss_button)).check(matches(isDisplayed()))
    }
}

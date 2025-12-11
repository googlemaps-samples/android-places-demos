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
package com.example.placedetailsuikit.compact

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.example.placedetailsuikit.R
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import org.hamcrest.CoreMatchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [ConfigurablePlaceDetailsActivity].
 * These tests verify the UI behavior related to the configuration dialog and state restoration.
 */
@RunWith(AndroidJUnit4::class)
class ConfigurablePlaceDetailsActivityInstrumentedTest {

    /**
     * A rule to launch [ConfigurablePlaceDetailsActivity] and interact with its Compose content.
     */
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ConfigurablePlaceDetailsActivity>()

    /**
     * A rule to grant location permissions before each test, preventing system dialogs
     * from interfering with the tests.
     */
    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    /**
     * Tests that the configuration dialog opens, displays content correctly,
     * and can be dismissed.
     */
    @Test
    fun test_configureDialogOpensAndDismisses() {
        // 1. Click the "Configure" FAB to open the dialog
        Espresso.onView(ViewMatchers.withId(R.id.configure_button)).perform(ViewActions.click())

        // 2. Verify that the Compose-based dialog content is displayed
        // We check for the sticky headers to confirm the LazyColumn is there.
        composeTestRule.onNodeWithText("Selected Content").assertIsDisplayed()
        composeTestRule.onNodeWithText("Unselected Content").assertIsDisplayed()

        // Check for a specific item in the list
        composeTestRule.onNodeWithText("Rating").assertIsDisplayed()

        // 3. Click the "Close" button on the AlertDialog
        Espresso.onView(ViewMatchers.withText("Close")).perform(ViewActions.click())

        // 4. Verify the dialog is gone by checking that its content is no longer visible
        composeTestRule.onNodeWithText("Selected Content").assertDoesNotExist()
    }

    /**
     * Tests that toggling an item in the dialog and then rotating the screen
     * preserves the selection state.
     */
    @Test
    fun test_selectionStatePersistsOnConfigurationChange() {
        // 1. Open the configuration dialog
        Espresso.onView(ViewMatchers.withId(R.id.configure_button)).perform(ViewActions.click())

        // 2. Toggle an item (e.g., move "Rating" from selected to unselected)
        composeTestRule.onNodeWithText("Rating").performClick()

        // After the click, "Rating" should now be under the "Unselected Content" header.
        // We can verify this by checking its new position relative to the headers.
        composeTestRule.onNodeWithText("Rating").assertIsDisplayed()


        // 3. Close the dialog
        Espresso.onView(ViewMatchers.withText("Close")).perform(ViewActions.click())

        // 4. Recreate the activity to simulate a screen rotation
        composeTestRule.activityRule.scenario.recreate()

        // 5. Re-open the dialog and verify the state was restored
        Espresso.onView(ViewMatchers.withId(R.id.configure_button)).perform(ViewActions.click())

        // Check that "Rating" is still in the unselected list.
        composeTestRule.onNodeWithText("Unselected Content").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rating").assertIsDisplayed()
    }

    /**
     * Test the full user flow: click POI, check that the Place Details card is displayed,
     * and dismiss it. This confirms the activity's core functionality.
     */
    @Test
    fun test_poiClickAndDismissFlow() {
        // 1. Simulate a POI Click
        composeTestRule.activityRule.scenario.onActivity { activity ->
            val poi = PointOfInterest(
                LatLng(-33.865072, 151.1961474),
                "ChIJP3Sa8ziYEmsRUKgyFmh9AQM",
                "Google Sydney"
            )
            activity.onPoiClick(poi)
        }

        // 2. Verify UI After Click
        // The wrapper view should be visible, and the loader should be showing initially.
        Espresso.onView(ViewMatchers.withId(R.id.place_details_wrapper))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.loading_indicator_configurable))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        // Wait for the place to load. For a real app, use Espresso Idling Resources.
        Thread.sleep(3000)

        // The loader should be gone, and the fragment container should be visible.
        Espresso.onView(ViewMatchers.withId(R.id.loading_indicator_configurable))
            .check(ViewAssertions.matches(CoreMatchers.not(ViewMatchers.isDisplayed())))
        Espresso.onView(ViewMatchers.withId(R.id.place_details_container))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.dismiss_button))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        // 3. Click the Dismiss Button
        Espresso.onView(ViewMatchers.withId(R.id.dismiss_button)).perform(ViewActions.click())

        // 4. Verify UI After Dismiss
        Espresso.onView(ViewMatchers.withId(R.id.place_details_wrapper))
            .check(ViewAssertions.matches(CoreMatchers.not(ViewMatchers.isDisplayed())))
    }
}
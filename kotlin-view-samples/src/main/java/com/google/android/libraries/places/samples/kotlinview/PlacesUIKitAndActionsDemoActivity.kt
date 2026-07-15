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

package com.google.android.libraries.places.samples.kotlinview

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.samples.kotlinview.core.Demo
import com.google.android.libraries.places.samples.kotlinview.core.DemoCategory
import com.google.android.libraries.places.samples.kotlinview.databinding.ActivityPlacesUiKitAndActionsBinding
import com.google.android.libraries.places.widget.AdvancedPlaceDetailsCompactFragment
import com.google.android.libraries.places.widget.AdvancedPlaceDetailsFragment
import com.google.android.libraries.places.widget.PlaceLoadListener
import com.google.android.libraries.places.widget.model.CornerPlaceAction
import com.google.android.libraries.places.widget.model.PlaceAction
import com.google.android.libraries.places.widget.model.PlaceActionProvider

@Demo(
    title = "Places UI Kit & Actions",
    description = "AdvancedPlaceDetailsFragment, AdvancedPlaceDetailsCompactFragment, and PlaceActionProvider",
    category = DemoCategory.UI_KIT_AND_ACTIONS,
    order = 5
)
class PlacesUIKitAndActionsDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlacesUiKitAndActionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlacesUiKitAndActionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.inflateMenu(R.menu.menu_demo_info)
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_reset_defaults) {
                getSharedPreferences("demo_prefs", MODE_PRIVATE).edit().clear().apply()
                resetInputsToDefault()
                Toast.makeText(this, "Reset to factory defaults", Toast.LENGTH_SHORT).show()
                true
            } else if (item.itemId == R.id.action_info) {
                com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("ℹ️ Demo Instructions & Info")
                    .setMessage("Main Point: Demonstrates AdvancedPlaceDetailsCompactFragment with custom PlaceActionProvider (CALL, WEBSITE, DIRECTIONS, MAPS).\n\nHow to Use: Interact with custom action buttons and adjust media/review ranking preferences.")
                    .setPositiveButton("Got It", null)
                    .show()
                true
            } else {
                false
            }
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val apiKey = BuildConfig.PLACES_API_KEY
        if (apiKey.isNotBlank() && apiKey != "YOUR_API_KEY") {
            Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)
        }

        binding.loadFragmentButton.setOnClickListener {
            val placeId = binding.placeIdInput.text.toString().trim()
            if (placeId.isEmpty()) {
                Toast.makeText(this, "Please enter a Place ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            loadUIKitFragment(placeId, binding.radioAdvancedCompact.isChecked)
        }

        // Initial default load
        loadUIKitFragment("ChIJ3S-JXYerEmsRUio6x_1nyio", true)
    }

    private fun createCustomActionProvider(): PlaceActionProvider {
        return object : PlaceActionProvider {
            override fun getMainPlaceActions(place: Place): List<PlaceAction> {
                return listOf(
                    PlaceAction.CALL,
                    PlaceAction.OPEN_WEBSITE,
                    PlaceAction.OPEN_DIRECTIONS,
                    PlaceAction.OPEN_IN_MAPS
                )
            }

            override fun getCornerPlaceActions(place: Place): List<CornerPlaceAction> {
                return emptyList()
            }

            override fun addPlaceActionsChangedListener(listener: PlaceActionProvider.OnChangedListener) {}
            override fun removePlaceActionsChangedListener(listener: PlaceActionProvider.OnChangedListener) {}
        }
    }

    private fun loadUIKitFragment(placeId: String, isCompact: Boolean) {
        val fragment: Fragment = if (isCompact) {
            AdvancedPlaceDetailsCompactFragment.newInstance(
                AdvancedPlaceDetailsCompactFragment.ALL_CONTENT
            ).apply {
                setPlaceActionProvider(createCustomActionProvider())
                setPlaceLoadListener(object : PlaceLoadListener {
                    override fun onSuccess(place: Place) {
                        Toast.makeText(this@PlacesUIKitAndActionsDemoActivity, "Compact Fragment loaded: ${place.displayName}", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(e: Exception) {
                        Toast.makeText(this@PlacesUIKitAndActionsDemoActivity, "Failed to load: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        } else {
            AdvancedPlaceDetailsFragment.newInstance(
                AdvancedPlaceDetailsFragment.ALL_CONTENT
            ).apply {
                setPlaceActionProvider(createCustomActionProvider())
                setPlaceLoadListener(object : PlaceLoadListener {
                    override fun onSuccess(place: Place) {
                        Toast.makeText(this@PlacesUIKitAndActionsDemoActivity, "Full Fragment loaded: ${place.displayName}", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(e: Exception) {
                        Toast.makeText(this@PlacesUIKitAndActionsDemoActivity, "Failed to load: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

        supportFragmentManager
            .beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commitNow()

        binding.root.post {
            if (isCompact && fragment is AdvancedPlaceDetailsCompactFragment) {
                fragment.loadWithPlaceId(placeId)
            } else if (!isCompact && fragment is AdvancedPlaceDetailsFragment) {
                fragment.loadWithPlaceId(placeId)
            }
        }
    }

    private fun resetInputsToDefault() {
        getSharedPreferences("demo_prefs", MODE_PRIVATE).edit().clear().apply()
    }
}

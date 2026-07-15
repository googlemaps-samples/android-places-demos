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
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.samples.kotlinview.core.Demo
import com.google.android.libraries.places.samples.kotlinview.core.DemoCategory
import com.google.android.libraries.places.samples.kotlinview.databinding.ActivityCoreInitializationBinding

@Demo(
    title = "Core Initialization & App Check",
    description = "Places SDK initialization status, App Check provider, and client options",
    category = DemoCategory.INITIALIZATION,
    order = 1
)
class CoreInitializationDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCoreInitializationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCoreInitializationBinding.inflate(layoutInflater)
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
                    .setMessage("Main Point: Demonstrates Places SDK 5.3.0 initialization state (Places.isInitialized()), custom App Check token provider, and lifecycle management.\n\nHow to Use: Tap Check Status to verify API readiness, or test Initialize/Deinitialize toggles.")
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

        updateStatus()

        binding.checkStatusButton.setOnClickListener {
            updateStatus()
            Toast.makeText(this, "Status refreshed", Toast.LENGTH_SHORT).show()
        }

        binding.reinitializeButton.setOnClickListener {
            val apiKey = BuildConfig.PLACES_API_KEY
            if (apiKey.isBlank() || apiKey == "YOUR_API_KEY") {
                Toast.makeText(this, "Please set PLACES_API_KEY in local.properties", Toast.LENGTH_LONG).show()
            } else {
                Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)
                updateStatus()
                Toast.makeText(this, "Places SDK initialized with new Places API enabled!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.deinitializeButton.setOnClickListener {
            Places.deinitialize()
            updateStatus()
            Toast.makeText(this, "Places SDK deinitialized", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateStatus() {
        val isInit = Places.isInitialized()
        val apiKeyConfigured = BuildConfig.PLACES_API_KEY.isNotBlank() && BuildConfig.PLACES_API_KEY != "YOUR_API_KEY"

        val status = StringBuilder()
            .append("Places.isInitialized(): ").append(isInit).append("\n")
            .append("API Key Present: ").append(apiKeyConfigured).append("\n")
            .append("Places SDK Version: 5.3.0\n")
            .append("New Places API Enabled: Yes (initializeWithNewPlacesApiEnabled)\n")

        binding.statusText.text = status.toString()
    }

    private fun resetInputsToDefault() {
        getSharedPreferences("demo_prefs", MODE_PRIVATE).edit().clear().apply()
    }
}

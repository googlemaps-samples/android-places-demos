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

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.google.android.libraries.places.samples.kotlinview.core.Demo
import com.google.android.libraries.places.samples.kotlinview.core.DemoCategory
import com.google.android.libraries.places.samples.kotlinview.core.LocationHelper
import com.google.android.libraries.places.samples.kotlinview.databinding.ActivitySearchAndDiscoveryBinding
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode

@Demo(
    title = "Search & Discovery",
    description = "searchByText, searchNearby, findCurrentPlace, and Autocomplete",
    category = DemoCategory.SEARCH_AND_DISCOVERY,
    order = 2
)
class SearchAndDiscoveryDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchAndDiscoveryBinding
    private lateinit var placesClient: PlacesClient

    private val autocompleteLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val place = Autocomplete.getPlaceFromIntent(result.data!!)
                val sb = StringBuilder()
                sb.append("=== AUTOCOMPLETE RESULT ===\n")
                sb.append("ID: ").append(place.id).append("\n")
                sb.append("Name: ").append(place.displayName).append("\n")
                sb.append("Address: ").append(place.formattedAddress).append("\n")
                sb.append("LatLng: ").append(place.location).append("\n")
                binding.resultsTextView.text = sb.toString()
            } else if (result.resultCode == AutocompleteActivity.RESULT_ERROR && result.data != null) {
                val status = Autocomplete.getStatusFromIntent(result.data!!)
                binding.resultsTextView.text = "Autocomplete Error: ${status.statusMessage}"
            }
        }

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                executeFindCurrentPlace()
            } else {
                binding.resultsTextView.text = "Location permission denied. Cannot execute findCurrentPlace."
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchAndDiscoveryBinding.inflate(layoutInflater)
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
                    .setMessage("Main Point: Demonstrates searchByText, searchNearby, findCurrentPlace, and Autocomplete predictions.\n\nHow to Use: Tap category chips or search input to execute live queries. Current Place automatically requests location permission or uses preset fallbacks.")
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

        placesClient = Places.createClient(this)

        val presetNames = LocationHelper.PRESETS.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, presetNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.presetSpinner.adapter = adapter

        binding.executeSearchButton.setOnClickListener {
            val selectedPreset = LocationHelper.PRESETS[binding.presetSpinner.selectedItemPosition]
            val query = binding.queryInput.text.toString().trim()

            when {
                binding.radioSearchByText.isChecked -> performSearchByText(query, selectedPreset.latLng)
                binding.radioSearchNearby.isChecked -> performSearchNearby(selectedPreset.latLng)
                binding.radioFindCurrentPlace.isChecked -> performFindCurrentPlace()
                binding.radioAutocomplete.isChecked -> launchAutocomplete()
            }
        }
    }

    private fun getStandardPlaceFields(): List<Place.Field> {
        return listOf(
            Place.Field.ID,
            Place.Field.DISPLAY_NAME,
            Place.Field.FORMATTED_ADDRESS,
            Place.Field.RATING,
            Place.Field.LOCATION
        )
    }

    private fun performSearchByText(query: String, center: com.google.android.gms.maps.model.LatLng) {
        if (query.isEmpty()) {
            Toast.makeText(this, "Please enter a search query", Toast.LENGTH_SHORT).show()
            return
        }
        binding.resultsTextView.text = "Searching by text: '$query' near ${center.latitude}, ${center.longitude}..."

        val bounds = CircularBounds.newInstance(center, 5000.0)
        val request = SearchByTextRequest.builder(query, getStandardPlaceFields())
            .setLocationBias(bounds)
            .setMaxResultCount(10)
            .build()

        placesClient.searchByText(request)
            .addOnSuccessListener { response ->
                val sb = StringBuilder("=== searchByText Results (${response.places.size}) ===\n\n")
                response.places.forEachIndexed { index, place ->
                    sb.append("#${index + 1}: ${place.displayName}\n")
                    sb.append("  ID: ${place.id}\n")
                    sb.append("  Address: ${place.formattedAddress}\n")
                    sb.append("  Rating: ${place.rating ?: "N/A"}\n\n")
                }
                binding.resultsTextView.text = sb.toString()
            }
            .addOnFailureListener { e ->
                binding.resultsTextView.text = "searchByText failed: ${e.message}"
            }
    }

    private fun performSearchNearby(center: com.google.android.gms.maps.model.LatLng) {
        binding.resultsTextView.text = "Searching nearby (restaurants) near ${center.latitude}, ${center.longitude}..."

        val restriction = CircularBounds.newInstance(center, 3000.0)
        val request = SearchNearbyRequest.builder(restriction, getStandardPlaceFields())
            .setIncludedTypes(listOf("restaurant", "cafe"))
            .setMaxResultCount(10)
            .build()

        placesClient.searchNearby(request)
            .addOnSuccessListener { response ->
                val sb = StringBuilder("=== searchNearby Results (${response.places.size}) ===\n\n")
                response.places.forEachIndexed { index, place ->
                    sb.append("#${index + 1}: ${place.displayName}\n")
                    sb.append("  ID: ${place.id}\n")
                    sb.append("  Address: ${place.formattedAddress}\n")
                    sb.append("  Rating: ${place.rating ?: "N/A"}\n\n")
                }
                binding.resultsTextView.text = sb.toString()
            }
            .addOnFailureListener { e ->
                binding.resultsTextView.text = "searchNearby failed: ${e.message}"
            }
    }

    private fun performFindCurrentPlace() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            binding.resultsTextView.text = "Requesting location permission..."
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }
        executeFindCurrentPlace()
    }

    @SuppressLint("MissingPermission")
    private fun executeFindCurrentPlace() {
        binding.resultsTextView.text = "Finding current place..."

        val request = FindCurrentPlaceRequest.newInstance(getStandardPlaceFields())
        placesClient.findCurrentPlace(request)
            .addOnSuccessListener { response ->
                val sb = StringBuilder("=== findCurrentPlace Results (${response.placeLikelihoods.size}) ===\n\n")
                response.placeLikelihoods.forEachIndexed { index, likelihood ->
                    val place = likelihood.place
                    sb.append("#${index + 1}: ${place.displayName} (Likelihood: ${"%.2f".format(likelihood.likelihood)})\n")
                    sb.append("  ID: ${place.id}\n")
                    sb.append("  Address: ${place.formattedAddress}\n\n")
                }
                binding.resultsTextView.text = sb.toString()
            }
            .addOnFailureListener { e ->
                binding.resultsTextView.text = "findCurrentPlace failed: ${e.message}"
            }
    }

    private fun launchAutocomplete() {
        val intent = Autocomplete.IntentBuilder(
            AutocompleteActivityMode.OVERLAY,
            getStandardPlaceFields()
        ).build(this)
        autocompleteLauncher.launch(intent)
    }

    private fun resetInputsToDefault() {
        getSharedPreferences("demo_prefs", MODE_PRIVATE).edit().clear().apply()
    }
}

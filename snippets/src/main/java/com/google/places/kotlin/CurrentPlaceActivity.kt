// Copyright 2023 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.places.kotlin

import android.Manifest.permission
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.common_ui.catalog.Complexity
import com.example.common_ui.catalog.Framework
import com.example.common_ui.catalog.Sample
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.google.places.databinding.ActivityCurrentPlaceBinding
import com.google.places.databinding.ListItemPlaceBinding

@Sample(
    id = "com.google.places.kotlin.CurrentPlaceActivity",
    title = "Snippet: Nearby Search (SearchNearby)",
    description = "DevSite documentation snippet for searching nearby places within CircularBounds.",
    category = "Documentation Snippets",
    complexity = Complexity.SNIPPET,
    tags = ["#snippet", "#nearby", "#searchnearby"],
    apiCalls = [
        "CircularBounds.newInstance(center, radius)",
        "SearchNearbyRequest.builder(locationRestriction, placeFields).build()",
        "PlacesClient.searchNearby(request)",
    ],
    purpose = "Canonical DevSite code snippet demonstrating how to search for nearby places around a coordinate using SearchNearbyRequest.",
    successCriteria = "• Activity launches cleanly and demonstrates the SearchNearbyRequest snippet.",
    failureIndicators = "• Crash on launch.",
    framework = Framework.KOTLIN_VIEWS,
)
class CurrentPlaceActivity : AppCompatActivity() {

    private lateinit var placesClient: PlacesClient
    private lateinit var binding: ActivityCurrentPlaceBinding
    private lateinit var adapter: PlacesAdapter

    private val requestPermissionLauncher =
        registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            if (isGranted) {
                findCurrentPlace()
            } else {
                // Handle permission denied
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCurrentPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "$title (Kotlin)"

        WindowCompat.setDecorFitsSystemWindows(window, false)

        placesClient = (application as MainApplication).getPlacesClient()

        binding.currentPlaceButton.setOnClickListener { findCurrentPlace() }

        // Set up the RecyclerView
        adapter = PlacesAdapter()
        binding.placesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.placesRecyclerView.adapter = adapter
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun findCurrentPlace() {
        binding.progressBar.visibility = View.VISIBLE

        // [START maps_places_current_place]
        // Use fields to define the data types to return.
        val placeFields = listOf(Place.Field.DISPLAY_NAME, Place.Field.FORMATTED_ADDRESS, Place.Field.LOCATION)

        // Define the search area around the current location (500m radius).
        val currentLocation = LatLng(37.4220, -122.0841)
        val circle = CircularBounds.newInstance(currentLocation, 500.0)
        val request = SearchNearbyRequest.builder(circle, placeFields)
            .setMaxResultCount(20)
            .build()

        // Call searchNearby and handle the response (first check that the user has granted permission).
        if (ContextCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            placesClient.searchNearby(request).addOnSuccessListener { response ->
                binding.progressBar.visibility = View.GONE
                adapter.setPlaces(response.places)
            }.addOnFailureListener { exception ->
                binding.progressBar.visibility = View.GONE
                if (exception is ApiException) {
                    Log.e(TAG, "Place not found: ${exception.statusCode}")
                }
            }
        } else {
            // [START_EXCLUDE silent]
            binding.progressBar.visibility = View.GONE
            // [END_EXCLUDE]
            // A local method to request required permissions;
            // See https://developer.android.com/training/permissions/requesting
            getLocationPermission()
        }
        // [END maps_places_current_place]
    }

    private fun getLocationPermission() {
        requestPermissionLauncher.launch(permission.ACCESS_FINE_LOCATION)
    }

    // Adapter for the RecyclerView
    private class PlacesAdapter : RecyclerView.Adapter<PlacesAdapter.ViewHolder>() {

        private var places: List<Place> = emptyList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ListItemPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val place = places[position]
            holder.bind(place)
        }

        override fun getItemCount(): Int = places.size

        fun setPlaces(places: List<Place>) {
            this.places = places
            notifyDataSetChanged()
        }

        class ViewHolder(private val binding: ListItemPlaceBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(place: Place) {
                binding.placeName.text = place.displayName
                binding.placeAddress.text = place.formattedAddress
                binding.placeLikelihood.visibility = View.GONE
            }
        }
    }

    companion object {
        private val TAG = CurrentPlaceActivity::class.java.simpleName
    }
}

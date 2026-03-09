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
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceLikelihood
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.places.databinding.ActivityCurrentPlaceBinding
import com.google.places.databinding.ListItemPlaceBinding
import java.util.Locale

class CurrentPlaceActivity : AppCompatActivity() {

    private lateinit var placesClient: PlacesClient
    private lateinit var binding: ActivityCurrentPlaceBinding
    private lateinit var adapter: PlacesAdapter

    private val requestPermissionLauncher =
        registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
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
        val request = FindCurrentPlaceRequest.newInstance(placeFields)

        // Call findCurrentPlace and handle the response (first check that the user has granted permission).
        if (ContextCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            placesClient.findCurrentPlace(request).addOnSuccessListener { response ->
                binding.progressBar.visibility = View.GONE
                adapter.setPlaceLikelihoods(response.placeLikelihoods)
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

        private var placeLikelihoods: List<PlaceLikelihood> = emptyList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ListItemPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val placeLikelihood = placeLikelihoods[position]
            holder.bind(placeLikelihood)
        }

        override fun getItemCount(): Int = placeLikelihoods.size

        fun setPlaceLikelihoods(placeLikelihoods: List<PlaceLikelihood>) {
            this.placeLikelihoods = placeLikelihoods
            notifyDataSetChanged()
        }

        class ViewHolder(private val binding: ListItemPlaceBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(placeLikelihood: PlaceLikelihood) {
                val place = placeLikelihood.place
                binding.placeName.text = place.displayName
                binding.placeAddress.text = place.formattedAddress
                binding.placeLikelihood.text = String.format(Locale.getDefault(), "Likelihood: %.2f", placeLikelihood.likelihood)
            }
        }
    }

    companion object {
        private val TAG = CurrentPlaceActivity::class.java.simpleName
    }
}
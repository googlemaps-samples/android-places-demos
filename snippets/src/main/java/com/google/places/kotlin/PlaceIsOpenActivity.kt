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

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.IsOpenRequest
import com.google.android.libraries.places.api.net.IsOpenResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.places.R
import com.google.places.data.PlaceIdProvider
import com.google.places.databinding.ActivityPlaceIsOpenBinding
import java.util.Calendar

class PlaceIsOpenActivity : AppCompatActivity() {
    private lateinit var placesClient: PlacesClient
    private lateinit var binding: ActivityPlaceIsOpenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaceIsOpenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "$title (Kotlin)"

        placesClient = (application as MainApplication).getPlacesClient()

        binding.isOpenByObjectButton.setOnClickListener {
            isOpenByPlaceObject()
        }
        binding.isOpenByIdButton.setOnClickListener {
            isOpenByPlaceId()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Check if the place is open at the time specified in the input fields.
     * Requires a Place object that includes Place.Field.ID
     */
    @SuppressLint("SetTextI18n")
    private fun isOpenByPlaceObject() {
        // [START maps_places_place_is_open]
        val isOpenCalendar: Calendar = Calendar.getInstance()
        var place: Place
        val placeId = PlaceIdProvider.getRandomPlaceId()
        // Specify the required fields for an isOpen request.
        val placeFields: List<Place.Field> = listOf(
            Place.Field.BUSINESS_STATUS,
            Place.Field.CURRENT_OPENING_HOURS,
            Place.Field.ID,
            Place.Field.OPENING_HOURS,
            Place.Field.DISPLAY_NAME
        )

        val placeRequest: FetchPlaceRequest =
            FetchPlaceRequest.newInstance(placeId, placeFields)
        val placeTask: Task<FetchPlaceResponse> = placesClient.fetchPlace(placeRequest)
        placeTask.addOnSuccessListener { placeResponse ->
            place = placeResponse.place

            val isOpenRequest: IsOpenRequest = try {
                IsOpenRequest.newInstance(place, isOpenCalendar.timeInMillis)
            } catch (e: IllegalArgumentException) {
                Log.e("PlaceIsOpen", "Error: " + e.message)
                return@addOnSuccessListener
            }
            val isOpenTask: Task<IsOpenResponse> = placesClient.isOpen(isOpenRequest)
            isOpenTask.addOnSuccessListener { isOpenResponse ->
                val isOpen = when (isOpenResponse.isOpen) {
                    true -> getString(R.string.is_open)
                    else -> getString(R.string.is_closed)
                }
                binding.isOpenByObjectResult.text = getString(
                    R.string.is_open_by_object,
                    place.displayName,
                    isOpen
                )
                Log.d("PlaceIsOpen", "Is open by object: $isOpen")
            }
            // [START_EXCLUDE]
            isOpenTask.addOnFailureListener { exception ->
                Log.e("PlaceIsOpen", "Error: " + exception.message)
            }
            // [END_EXCLUDE]
        }
        // [START_EXCLUDE]
        placeTask.addOnFailureListener { exception ->
            Log.e("PlaceIsOpen", "Error: " + exception.message)
        }
        // [END_EXCLUDE]
        // [END maps_places_place_is_open]
    }

    /**
     * Check if the place is open at the time specified in the input fields.
     * Use the Place ID in the input field for the isOpenRequest.
     */
    @SuppressLint("SetTextI18n")
    private fun isOpenByPlaceId() {
        // [START maps_places_id_is_open]
        val isOpenCalendar: Calendar = Calendar.getInstance()
        val placeId = PlaceIdProvider.getRandomPlaceId()

        val request: IsOpenRequest = try {
            IsOpenRequest.newInstance(placeId, isOpenCalendar.timeInMillis)
        } catch (e: IllegalArgumentException) {
            Log.e("PlaceIsOpen", "Error: " + e.message)
            return
        }
        val isOpenTask: Task<IsOpenResponse> = placesClient.isOpen(request)
        isOpenTask.addOnSuccessListener { response ->
            val isOpen = response.isOpen ?: false
            binding.isOpenByIdResult.text = getString(R.string.is_open_by_id, isOpen.toString())
            Log.d("PlaceIsOpen", "Is open by ID: $isOpen")
        }
        // [START_EXCLUDE]
        isOpenTask.addOnFailureListener { exception ->
            Log.e("PlaceIsOpen", "Error: " + exception.message)
        }
        isOpenTask.addOnCompleteListener { }
        // [END_EXCLUDE]
        // [END maps_places_id_is_open]
    }
}
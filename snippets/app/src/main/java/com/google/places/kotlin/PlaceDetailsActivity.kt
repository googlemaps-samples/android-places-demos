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

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.places.R
import com.google.places.data.PlaceIdProvider
import com.google.places.databinding.ActivityPlaceDetailsBinding

class PlaceDetailsActivity : AppCompatActivity() {
    private lateinit var placesClient: PlacesClient
    private lateinit var binding: ActivityPlaceDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaceDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "$title (Kotlin)"

        WindowCompat.setDecorFitsSystemWindows(window, false)

        placesClient = (application as MainApplication).getPlacesClient()

        getPlaceById()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getPlaceById() {
        // [START maps_places_get_place_by_id]
        // Define a Place ID.
        val placeId = PlaceIdProvider.getRandomPlaceId()

        // Specify the fields to return.
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.DISPLAY_NAME,
            Place.Field.FORMATTED_ADDRESS,
            Place.Field.LOCATION
        )

        // Construct a request object, passing the place ID and fields array.
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response: FetchPlaceResponse ->
                val place = response.place

                // [START maps_places_place_details_simple]
                val name = place.displayName
                val address = place.formattedAddress
                val location = place.location
                // [END maps_places_place_details_simple]

                binding.placeName.text = name
                binding.placeAddress.text = address
                if (location != null) {
                    binding.placeLocation.text = getString(
                        R.string.place_location, location.latitude, location.longitude
                    )
                } else {
                    binding.placeLocation.text = null
                }
                Log.i(TAG, "Place found: ${place.displayName}")
            }.addOnFailureListener { exception: Exception ->
                if (exception is ApiException) {
                    val message = getString(R.string.place_not_found, exception.message)
                    binding.placeName.text = message
                    Log.e(TAG, "Place not found: ${exception.message}")
                    val statusCode = exception.statusCode
                    TODO("Handle error with given status code")
                }
            }
        // [END maps_places_get_place_by_id]
    }

    companion object {
        private val TAG = PlaceDetailsActivity::class.java.simpleName
    }
}
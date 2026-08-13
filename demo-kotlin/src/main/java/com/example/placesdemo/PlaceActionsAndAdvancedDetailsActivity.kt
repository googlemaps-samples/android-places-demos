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

import android.os.Bundle
import android.widget.Toast
import com.example.placesdemo.databinding.ActivityPlaceActionsAdvancedBinding
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.model.CornerPlaceAction
import com.google.android.libraries.places.widget.model.PlaceAction
import com.google.android.libraries.places.widget.model.PlaceActionProvider
import com.google.android.libraries.places.widget.model.SearchMediaOptions
import com.google.android.libraries.places.widget.model.SearchReviewsOptions

/**
 * Activity demonstrating Places SDK 5.3.0 features:
 * - AddressDescriptor (Areas and Landmarks)
 * - ContainingPlace
 * - PriceRange
 * - SearchMediaOptions and SearchReviewsOptions
 * - PlaceActionProvider / PlaceAction / CornerPlaceAction configuration
 */
class PlaceActionsAndAdvancedDetailsActivity : BaseActivity() {

    private lateinit var placesClient: PlacesClient
    private lateinit var binding: ActivityPlaceActionsAdvancedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaceActionsAdvancedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.topBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        placesClient = Places.createClient(this)

        binding.fetchAdvancedButton.setOnClickListener {
            val placeId = binding.inputPlaceId.text.toString().trim()
            if (placeId.isEmpty()) {
                Toast.makeText(this, "Please enter a Place ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            fetchAdvancedDetailsAndShowActions(placeId)
        }
    }

    private fun fetchAdvancedDetailsAndShowActions(placeId: String) {
        binding.responseText.text = "Fetching details for Place ID: $placeId..."

        // 1. Define fields including 5.3.0 new data models
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.DISPLAY_NAME,
            Place.Field.FORMATTED_ADDRESS,
            Place.Field.ADDRESS_DESCRIPTOR,
            Place.Field.CONTAINING_PLACES,
            Place.Field.PRICE_RANGE
        )

        val request = FetchPlaceRequest.builder(placeId, placeFields).build()

        placesClient.fetchPlace(request).addOnSuccessListener { response ->
            val place = response.place
            val sb = StringBuilder()
            sb.append("=== PLACE DETAILS (5.3.0) ===\n")
            sb.append("Name: ${place.displayName}\n")
            sb.append("Formatted Address: ${place.formattedAddress}\n\n")

            // AddressDescriptor check
            place.addressDescriptor?.let { descriptor ->
                sb.append("--- AddressDescriptor ---\n")
                descriptor.areas?.let { areas ->
                    sb.append("Areas (${areas.size}):\n")
                    areas.forEach { area ->
                        sb.append("  * ${area.displayName} (${area.containment})\n")
                    }
                }
                descriptor.landmarks?.let { landmarks ->
                    sb.append("Landmarks (${landmarks.size}):\n")
                    landmarks.forEach { landmark ->
                        sb.append("  * ${landmark.displayName} (${landmark.spatialRelationship})\n")
                    }
                }
                sb.append("\n")
            } ?: sb.append("AddressDescriptor: None available for this place\n\n")

            // ContainingPlace check
            place.containingPlaces?.let { containingList ->
                sb.append("--- Containing Places ---\n")
                containingList.forEach { cp ->
                    sb.append("  * ID: ${cp.id}, ResourceName: ${cp.resourceName}\n")
                }
                sb.append("\n")
            } ?: sb.append("Containing Places: None available for this place\n\n")

            // PriceRange check
            place.priceRange?.let { pr ->
                sb.append("--- Price Range ---\n")
                sb.append("Start: ${pr.startPrice?.units}.${pr.startPrice?.nanos}, End: ${pr.endPrice?.units}.${pr.endPrice?.nanos}\n\n")
            } ?: sb.append("Price Range: None available for this place\n\n")

            // 2. Demonstrate 5.3.0 SearchMediaOptions & SearchReviewsOptions
            val mediaOptions = SearchMediaOptions.builder()
                .setQuery("menu")
                .setRankPreference(SearchMediaOptions.RankPreference.MOST_RELEVANT)
                .build()

            val reviewsOptions = SearchReviewsOptions.builder()
                .setQuery("service")
                .setRankPreference(SearchReviewsOptions.RankPreference.HIGHEST_RATING)
                .build()

            sb.append("--- 5.3.0 Search Options ---\n")
            sb.append("SearchMediaOptions: query='${mediaOptions.query}', rank=${mediaOptions.rankPreference}\n")
            sb.append("SearchReviewsOptions: query='${reviewsOptions.query}', rank=${reviewsOptions.rankPreference}\n\n")

            // 3. Demonstrate 5.3.0 PlaceActionProvider and PlaceActions
            val actionProvider = object : PlaceActionProvider {
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

            val mainActions = actionProvider.getMainPlaceActions(place)
            sb.append("--- 5.3.0 Configured PlaceActions (${mainActions.size}) ---\n")
            mainActions.forEachIndexed { i, action ->
                sb.append("  Action #${i + 1}: labelResId=${action.labelTextResId}, style=${action.buttonStyle}\n")
            }

            binding.responseText.text = sb.toString()
        }.addOnFailureListener { exception ->
            binding.responseText.text = "Error fetching place: ${exception.message}"
        }
    }
}

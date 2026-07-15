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
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.IsOpenRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.samples.kotlinview.core.Demo
import com.google.android.libraries.places.samples.kotlinview.core.DemoCategory
import com.google.android.libraries.places.samples.kotlinview.databinding.ActivityPlaceAttributesAndHoursBinding

@Demo(
    title = "Place Attributes & Hours",
    description = "isOpen, price range, containing place, EV options, and accessibility",
    category = DemoCategory.ATTRIBUTES_AND_HOURS,
    order = 4
)
class PlaceAttributesAndHoursDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaceAttributesAndHoursBinding
    private lateinit var placesClient: PlacesClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaceAttributesAndHoursBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        placesClient = Places.createClient(this)

        binding.checkIsOpenButton.setOnClickListener {
            val placeId = binding.placeIdInput.text.toString().trim()
            if (placeId.isEmpty()) {
                Toast.makeText(this, "Please enter a Place ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            checkIsOpen(placeId)
        }

        binding.fetchAttributesButton.setOnClickListener {
            val placeId = binding.placeIdInput.text.toString().trim()
            if (placeId.isEmpty()) {
                Toast.makeText(this, "Please enter a Place ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            fetchAttributes(placeId)
        }
    }

    private fun checkIsOpen(placeId: String) {
        binding.attributesTextView.text = "Checking isOpen status for Place ID: $placeId..."

        val request = IsOpenRequest.newInstance(placeId, System.currentTimeMillis())
        placesClient.isOpen(request)
            .addOnSuccessListener { response ->
                binding.attributesTextView.text = "=== isOpen STATUS ===\nPlace ID: $placeId\nIs Open Now: ${response.isOpen}"
            }
            .addOnFailureListener { e ->
                binding.attributesTextView.text = "isOpen check failed: ${e.message}"
            }
    }

    private fun fetchAttributes(placeId: String) {
        binding.attributesTextView.text = "Fetching 5.3.0 Place Attributes for: $placeId..."

        val fields = listOf(
            Place.Field.ID,
            Place.Field.DISPLAY_NAME,
            Place.Field.PRICE_RANGE,
            Place.Field.CONTAINING_PLACES,
            Place.Field.EV_CHARGE_OPTIONS,
            Place.Field.ACCESSIBILITY_OPTIONS,
            Place.Field.OPENING_HOURS,
            Place.Field.CURRENT_OPENING_HOURS,
            Place.Field.BUSINESS_STATUS,
            Place.Field.RESERVABLE,
            Place.Field.SERVES_BEER,
            Place.Field.SERVES_BREAKFAST,
            Place.Field.SERVES_DINNER,
            Place.Field.SERVES_LUNCH,
            Place.Field.SERVES_VEGETARIAN_FOOD,
            Place.Field.SERVES_WINE
        )

        val request = FetchPlaceRequest.builder(placeId, fields).build()
        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val place = response.place
                val sb = StringBuilder("=== 5.3.0 PLACE ATTRIBUTES ===\n")
                sb.append("Name: ").append(place.displayName).append("\n")
                sb.append("Business Status: ").append(place.businessStatus).append("\n")
                sb.append("Accessibility Options: ").append(place.accessibilityOptions).append("\n")
                sb.append("Reservable: ").append(place.reservable).append("\n")
                sb.append("Serves Vegetarian Food: ").append(place.servesVegetarianFood).append("\n")
                sb.append("Serves Beer/Wine: ").append(place.servesBeer).append(" / ").append(place.servesWine).append("\n\n")

                // Price Range
                place.priceRange?.let { pr ->
                    sb.append("--- Price Range ---\n")
                    sb.append("Start: ").append(pr.startPrice?.units).append(" ").append(pr.startPrice?.currencyCode).append("\n")
                    sb.append("End: ").append(pr.endPrice?.units).append(" ").append(pr.endPrice?.currencyCode).append("\n\n")
                } ?: sb.append("Price Range: N/A\n\n")

                // Containing Places
                place.containingPlaces?.let { cps ->
                    sb.append("--- Containing Places (${cps.size}) ---\n")
                    cps.forEach { cp ->
                        sb.append("  * ID: ").append(cp.id).append(", Resource: ").append(cp.resourceName).append("\n")
                    }
                    sb.append("\n")
                } ?: sb.append("Containing Places: N/A\n\n")

                // EV Charge Options
                place.evChargeOptions?.let { ev ->
                    sb.append("--- EV Charge Options ---\n")
                    sb.append("Ports: ").append(ev.connectorCount).append("\n")
                    ev.connectorAggregations.forEach { agg ->
                        sb.append("  * Connector: ").append(agg.type).append(" (${agg.count} available, ").append(agg.maxChargeRateKw).append(" kW)\n")
                    }
                    sb.append("\n")
                } ?: sb.append("EV Charge Options: N/A\n\n")

                // Opening Hours
                place.openingHours?.let { oh ->
                    sb.append("--- Opening Hours ---\n")
                    oh.weekdayText.forEach { text ->
                        sb.append("  * ").append(text).append("\n")
                    }
                } ?: sb.append("Opening Hours: N/A\n")

                binding.attributesTextView.text = sb.toString()
            }
            .addOnFailureListener { e ->
                binding.attributesTextView.text = "Failed to fetch attributes: ${e.message}"
            }
    }
}

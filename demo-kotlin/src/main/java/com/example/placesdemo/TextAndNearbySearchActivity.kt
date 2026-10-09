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
package com.example.placesdemo

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import com.example.placesdemo.databinding.ActivityTextAndNearbySearchBinding
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest
import com.google.android.libraries.places.api.net.SearchNearbyRequest

/**
 * Activity demonstrating Places SDK (New) [SearchByTextRequest] and [SearchNearbyRequest] in Kotlin.
 *
 * ## Architectural Overview
 * The Places SDK (New) exposes two primary place discovery mechanisms on [PlacesClient]:
 * 1. **Text Search ([PlacesClient.searchByText])**: Evaluates a natural-language query string
 *    (such as `"Spicy Vegetarian Food in Sydney"`), optionally biased to a [CircularBounds] region,
 *    constrained to a specific `includedType`, and filtered by `isOpenNow`.
 * 2. **Nearby Search ([PlacesClient.searchNearby])**: Searches within an explicit [CircularBounds]
 *    restriction, optionally filtered by `includedTypes` and ranked by popularity or distance.
 *
 * Request construction is isolated in internal helper functions ([buildSearchByTextRequest] and
 * [buildSearchNearbyRequest]) so Robolectric unit tests can verify request building and input
 * validation deterministically without network dependencies.
 */
class TextAndNearbySearchActivity : BaseActivity() {

    private lateinit var placesClient: PlacesClient
    private lateinit var binding: ActivityTextAndNearbySearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextAndNearbySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.topBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        placesClient = Places.createClient(this)

        binding.searchByTextButton.setOnClickListener { executeSearchByText() }
        binding.searchNearbyButton.setOnClickListener { executeSearchNearby() }
    }

    /**
     * Validates inputs, constructs a [SearchByTextRequest], and executes [PlacesClient.searchByText].
     */
    private fun executeSearchByText() {
        val request = buildSearchByTextRequest() ?: return

        setLoading(true)
        binding.responseText.text = "Executing searchByText(\"${request.textQuery}\")..."

        placesClient.searchByText(request)
            .addOnSuccessListener { response ->
                binding.responseText.text =
                    StringUtil.stringify(response, binding.displayRawResults.isChecked)
            }
            .addOnFailureListener { exception ->
                binding.responseText.text = "SearchByText Error: ${exception.message}"
            }
            .addOnCompleteListener { setLoading(false) }
    }

    /**
     * Validates inputs, constructs a [SearchNearbyRequest], and executes [PlacesClient.searchNearby].
     */
    private fun executeSearchNearby() {
        val request = buildSearchNearbyRequest() ?: return

        setLoading(true)
        binding.responseText.text = "Executing searchNearby..."

        placesClient.searchNearby(request)
            .addOnSuccessListener { response ->
                binding.responseText.text =
                    StringUtil.stringify(response, binding.displayRawResults.isChecked)
            }
            .addOnFailureListener { exception ->
                binding.responseText.text = "SearchNearby Error: ${exception.message}"
            }
            .addOnCompleteListener { setLoading(false) }
    }

    /**
     * Builds a [SearchByTextRequest] from the current UI state, or returns `null` after displaying
     * a validation error in the UI.
     */
    @VisibleForTesting
    internal fun buildSearchByTextRequest(): SearchByTextRequest? {
        val query = binding.inputTextQuery.text.toString().trim()
        if (query.isEmpty()) {
            binding.responseText.setText(R.string.error_empty_text_query)
            Toast.makeText(this, R.string.error_empty_text_query, Toast.LENGTH_SHORT).show()
            return null
        }

        val bounds = parseCircularBounds() ?: return null

        val builder = SearchByTextRequest.builder(query, DEFAULT_SEARCH_FIELDS)
            .setLocationBias(bounds)
            .setOpenNow(binding.checkboxOpenNow.isChecked)
            .setMaxResultCount(DEFAULT_MAX_RESULTS)
            .setRankPreference(SearchByTextRequest.RankPreference.RELEVANCE)

        val includedType = binding.inputIncludedType.text.toString().trim()
        if (includedType.isNotEmpty()) {
            builder.setIncludedType(includedType)
        }

        return builder.build()
    }

    /**
     * Builds a [SearchNearbyRequest] from the current UI state, or returns `null` after displaying
     * a validation error in the UI.
     */
    @VisibleForTesting
    internal fun buildSearchNearbyRequest(): SearchNearbyRequest? {
        val bounds = parseCircularBounds() ?: return null

        val builder = SearchNearbyRequest.builder(bounds, DEFAULT_SEARCH_FIELDS)
            .setMaxResultCount(DEFAULT_MAX_RESULTS)
            .setRankPreference(SearchNearbyRequest.RankPreference.POPULARITY)

        val includedType = binding.inputIncludedType.text.toString().trim()
        if (includedType.isNotEmpty()) {
            builder.setIncludedTypes(listOf(includedType))
        }

        return builder.build()
    }

    private fun parseCircularBounds(): CircularBounds? {
        val center = StringUtil.convertToLatLng(binding.inputCenterLatLng.text.toString().trim())
        if (center == null) {
            binding.responseText.setText(R.string.error_alert_message_invalid_bounds)
            return null
        }

        val radiusStr = binding.inputRadiusMeters.text.toString().trim()
        val radius = if (radiusStr.isEmpty()) {
            DEFAULT_RADIUS_METERS
        } else {
            val parsed = radiusStr.toDoubleOrNull()
            if (parsed == null || parsed <= 0.0) {
                binding.responseText.setText(R.string.error_invalid_radius)
                return null
            }
            parsed
        }

        return CircularBounds.newInstance(center, radius)
    }

    private fun setLoading(loading: Boolean) {
        binding.loading.visibility = if (loading) View.VISIBLE else View.INVISIBLE
    }

    companion object {
        private const val DEFAULT_RADIUS_METERS = 2000.0
        private const val DEFAULT_MAX_RESULTS = 10

        @VisibleForTesting
        internal val DEFAULT_SEARCH_FIELDS = listOf(
            Place.Field.ID,
            Place.Field.DISPLAY_NAME,
            Place.Field.FORMATTED_ADDRESS,
            Place.Field.LOCATION,
            Place.Field.RATING,
            Place.Field.TYPES
        )
    }
}

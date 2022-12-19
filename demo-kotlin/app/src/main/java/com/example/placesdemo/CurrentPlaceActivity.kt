/*
 * Copyright 2022 Google LLC
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

import android.Manifest.permission
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.placesdemo.databinding.CurrentPlaceActivityBinding
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient

/**
 * Activity to demonstrate [PlacesClient.findCurrentPlace].
 */
class CurrentPlaceActivity : AppCompatActivity() {
    private lateinit var placesClient: PlacesClient
    private lateinit var fieldSelector: FieldSelector

    private lateinit var binding: CurrentPlaceActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = CurrentPlaceActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve a PlacesClient (previously initialized - see MainActivity)
        placesClient = Places.createClient(this)

        // Set view objects
        // Exclude fields that are not supported by search endpoints
        val placeFields = FieldSelector.allExcept(
            Place.Field.ADDRESS_COMPONENTS,
            Place.Field.CURBSIDE_PICKUP,
            Place.Field.DELIVERY,
            Place.Field.DINE_IN,
            Place.Field.OPENING_HOURS,
            Place.Field.PHONE_NUMBER,
            Place.Field.TAKEOUT,
            Place.Field.UTC_OFFSET,
            Place.Field.WEBSITE_URI
        )
        fieldSelector = FieldSelector(
            binding.useCustomFields,
            binding.customFieldsList,
            savedInstanceState,
            placeFields
        )
        setLoading(false)

        // Set listeners for programmatic Find Current Place
        binding.findCurrentPlaceButton.setOnClickListener {
            findCurrentPlace()
        }
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        fieldSelector.onSaveInstanceState(bundle)
    }

    /**
     * Fetches a list of [com.google.android.libraries.places.api.model.PlaceLikelihood] instances that represent the Places the user is
     * most
     * likely to be at currently.
     */
    private fun findCurrentPlace() {
        if (ContextCompat.checkSelfPermission(this, permission.ACCESS_WIFI_STATE)
            != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(
                this,
                "Both ACCESS_WIFI_STATE & ACCESS_FINE_LOCATION permissions are required",
                Toast.LENGTH_SHORT
            )
                .show()
        }

        // Note that it is not possible to request a normal (non-dangerous) permission from
        // ActivityCompat.requestPermissions(), which is why the checkPermission() only checks if
        // ACCESS_FINE_LOCATION is granted. It is still possible to check whether a normal permission
        // is granted or not using ContextCompat.checkSelfPermission().
        if (ContextCompat.checkSelfPermission(
                this,
                permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(permission.ACCESS_FINE_LOCATION), 0)
            return
        }
        findCurrentPlaceWithPermissions()
    }

    /**
     * Fetches a list of [com.google.android.libraries.places.api.model.PlaceLikelihood] instances that represent the Places the user is
     * most likely to be at currently.
     */
    @RequiresPermission(allOf = [permission.ACCESS_FINE_LOCATION, permission.ACCESS_WIFI_STATE])
    private fun findCurrentPlaceWithPermissions() {
        setLoading(true)
        val currentPlaceRequest = FindCurrentPlaceRequest.newInstance(placeFields)
        val currentPlaceTask = placesClient.findCurrentPlace(currentPlaceRequest)
        currentPlaceTask.addOnSuccessListener { response: FindCurrentPlaceResponse? ->
            response?.let {
                binding.response.text = StringUtil.stringify(it, isDisplayRawResultsChecked)
            }
        }
        currentPlaceTask.addOnFailureListener { exception: Exception ->
            exception.printStackTrace()
            binding.response.text = exception.message
        }
        currentPlaceTask.addOnCompleteListener { setLoading(false) }
    }

    //////////////////////////
    // Helper methods below //
    //////////////////////////
    private val placeFields: List<Place.Field>
        get() = if (binding.useCustomFields.isChecked) {
            fieldSelector.selectedFields
        } else {
            fieldSelector.allFields
        }

    private val isDisplayRawResultsChecked: Boolean
        get() = binding.displayRawResults.isChecked

    private fun setLoading(loading: Boolean) {
        binding.loading.visibility = if (loading) View.VISIBLE else View.INVISIBLE
    }
}
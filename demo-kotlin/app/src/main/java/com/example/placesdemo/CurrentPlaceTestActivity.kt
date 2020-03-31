/*
 * Copyright 2018 Google LLC
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
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient

/**
 * Activity for testing [PlacesClient.findCurrentPlace].
 */
class CurrentPlaceTestActivity : AppCompatActivity() {
    private lateinit var placesClient: PlacesClient
    private lateinit var responseView: TextView
    private lateinit var fieldSelector: FieldSelector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use whatever theme was set from the MainActivity.
        val theme = intent.getIntExtra(MainActivity.THEME_RES_ID_EXTRA, 0)
        if (theme != 0) {
            setTheme(theme)
        }
        setContentView(R.layout.current_place_test_activity)

        // Retrieve a PlacesClient (previously initialized - see MainActivity)
        placesClient = Places.createClient(this)

        // Set view objects
        val placeFields = FieldSelector.allExcept(
            Place.Field.ADDRESS_COMPONENTS,
            Place.Field.OPENING_HOURS,
            Place.Field.PHONE_NUMBER,
            Place.Field.UTC_OFFSET,
            Place.Field.WEBSITE_URI)
        fieldSelector = FieldSelector(
            findViewById(R.id.use_custom_fields),
            findViewById(R.id.custom_fields_list),
            savedInstanceState,
            placeFields)
        responseView = findViewById(R.id.response)
        setLoading(false)

        // Set listeners for programmatic Find Current Place
        findViewById<Button>(R.id.find_current_place_button).setOnClickListener {
            findCurrentPlace()
        }
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        fieldSelector.onSaveInstanceState(bundle)
    }

    /**
     * Fetches a list of [PlaceLikelihood] instances that represent the Places the user is
     * most
     * likely to be at currently.
     */
    private fun findCurrentPlace() {
        if (ContextCompat.checkSelfPermission(this, permission.ACCESS_WIFI_STATE)
            != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(
                this,
                "Both ACCESS_WIFI_STATE & ACCESS_FINE_LOCATION permissions are required",
                Toast.LENGTH_SHORT)
                .show()
        }

        // Note that it is not possible to request a normal (non-dangerous) permission from
        // ActivityCompat.requestPermissions(), which is why the checkPermission() only checks if
        // ACCESS_FINE_LOCATION is granted. It is still possible to check whether a normal permission
        // is granted or not using ContextCompat.checkSelfPermission().
        if (ContextCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission.ACCESS_FINE_LOCATION), 0)
            return
        }
        findCurrentPlaceWithPermissions()
    }

    /**
     * Fetches a list of [PlaceLikelihood] instances that represent the Places the user is
     * most likely to be at currently.
     */
    @RequiresPermission(allOf = [permission.ACCESS_FINE_LOCATION, permission.ACCESS_WIFI_STATE])
    private fun findCurrentPlaceWithPermissions() {
        setLoading(true)
        val currentPlaceRequest = FindCurrentPlaceRequest.newInstance(placeFields)
        val currentPlaceTask = placesClient.findCurrentPlace(currentPlaceRequest)
        currentPlaceTask.addOnSuccessListener { response: FindCurrentPlaceResponse? ->
            response?.let {
                responseView.text = StringUtil.stringify(it, isDisplayRawResultsChecked)
            }
        }
        currentPlaceTask.addOnFailureListener { exception: Exception ->
            exception.printStackTrace()
            responseView.text = exception.message
        }
        currentPlaceTask.addOnCompleteListener { setLoading(false) }
    }

    //////////////////////////
    // Helper methods below //
    //////////////////////////
    private val placeFields: List<Place.Field>
        get() = if ((findViewById<View>(R.id.use_custom_fields) as CheckBox).isChecked) {
            fieldSelector.selectedFields
        } else {
            fieldSelector.allFields
        }

    private val isDisplayRawResultsChecked: Boolean
        get() = findViewById<CheckBox>(R.id.display_raw_results).isChecked

    private fun setLoading(loading: Boolean) {
        findViewById<View>(R.id.loading).visibility = if (loading) View.VISIBLE else View.INVISIBLE
    }
}
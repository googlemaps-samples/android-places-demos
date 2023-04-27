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
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.IsOpenRequest
import com.google.android.libraries.places.api.net.IsOpenResponse
import com.google.android.libraries.places.api.net.PlacesClient
import java.util.Calendar

class PlacesIsOpenActivity : AppCompatActivity() {
    private lateinit var placesClient: PlacesClient

    /**
     * Check if the place is open at the time specified in the input fields.
     * Requires a Place object that includes Place.Field.ID
     */
    @SuppressLint("SetTextI18n")
    private fun isOpenByPlaceObject() {
        // [START maps_places_place_is_open]
        var place: Place
        val placeId = "ChIJD3uTd9hx5kcR1IQvGfr8dbk"
        // Specify the required fields for an isOpen request.
        val placeFields: List<Place.Field> = listOf(
            Place.Field.BUSINESS_STATUS,
            Place.Field.CURRENT_OPENING_HOURS,
            Place.Field.ID,
            Place.Field.OPENING_HOURS,
            Place.Field.UTC_OFFSET
        )
        val isOpenCalendar: Calendar = Calendar.getInstance()

        val placeRequest: FetchPlaceRequest =
            FetchPlaceRequest.newInstance(placeId, placeFields)
        val placeTask: Task<FetchPlaceResponse> = placesClient.fetchPlace(placeRequest)
        placeTask.addOnSuccessListener { placeResponse ->
            place = placeResponse.place

            val isOpenRequest: IsOpenRequest = try {
                IsOpenRequest.newInstance(place, isOpenCalendar.timeInMillis)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                return@addOnSuccessListener
            }
            val isOpenTask: Task<IsOpenResponse> = placesClient.isOpen(isOpenRequest)
            isOpenTask.addOnSuccessListener { isOpenResponse ->
                val isOpen = isOpenResponse.isOpen
            }
            // [START_EXCLUDE]
            isOpenTask.addOnFailureListener { exception ->
                exception.printStackTrace()
            }
            isOpenTask.addOnCompleteListener { }
            // [END_EXCLUDE]
        }
        // [START_EXCLUDE]
        placeTask.addOnFailureListener { exception ->
            exception.printStackTrace()
        }
        placeTask.addOnCompleteListener { }
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
        val placeId = "ChIJD3uTd9hx5kcR1IQvGfr8dbk"
        val isOpenCalendar: Calendar = Calendar.getInstance()

        val request: IsOpenRequest = try {
            IsOpenRequest.newInstance(placeId, isOpenCalendar.timeInMillis)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            return
        }
        val isOpenTask: Task<IsOpenResponse> = placesClient.isOpen(request)
        isOpenTask.addOnSuccessListener { response ->
            val isOpen = response.isOpen
        }
        // [START_EXCLUDE]
        isOpenTask.addOnFailureListener { exception ->
            exception.printStackTrace()
        }
        isOpenTask.addOnCompleteListener { }
        // [END_EXCLUDE]
        // [END maps_places_id_is_open]
    }
}
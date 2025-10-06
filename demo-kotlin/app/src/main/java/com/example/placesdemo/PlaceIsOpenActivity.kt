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

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import com.example.placesdemo.StringUtil.stringify
import com.example.placesdemo.databinding.PlaceIsOpenActivityBinding
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.IsOpenRequest
import com.google.android.libraries.places.api.net.IsOpenResponse
import com.google.android.libraries.places.api.net.PlacesClient
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone.getAvailableIDs
import java.util.TimeZone.getDefault
import java.util.TimeZone.getTimeZone

/**
 * Activity to demonstrate [PlacesClient.isOpen].
 */
class PlaceIsOpenActivity : BaseActivity() {
    private val defaultTimeZone = getDefault()
    private val defaultTimeZoneID: String = defaultTimeZone.id

    private var isOpenCalendar: Calendar = Calendar.getInstance()

    private lateinit var placesClient: PlacesClient
    private lateinit var fieldSelector: FieldSelector
    private lateinit var binding: PlaceIsOpenActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = PlaceIsOpenActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.topBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Retrieve a PlacesClient (previously initialized - see MainActivity)
        placesClient = Places.createClient( /* context = */this)

        fieldSelector = FieldSelector(
            binding.checkBoxUseCustomFields,
            binding.textViewCustomFieldsList,
            savedInstanceState
        )

        binding.buttonFetchPlace.setOnClickListener { fetchPlace() }
        binding.buttonIsOpen.setOnClickListener { isOpenByPlaceId() }

        isOpenCalendar = Calendar.getInstance(defaultTimeZone)

        // UI initialization
        setLoading(false)
        initializeSpinnerAndAddListener()
        addIsOpenDateSelectionListener()
        addIsOpenTimeSelectionListener()
        updateIsOpenDate()
        updateIsOpenTime()
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        fieldSelector.onSaveInstanceState(bundle)
    }

    /**
     * Get details about the Place ID listed in the input field, then check if the Place is open.
     */
    private fun fetchPlace() {
        clearViews()
        dismissKeyboard(binding.editTextPlaceId)
        setLoading(true)

        val placeFields = this.placeFields
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)
        val placeTask = placesClient.fetchPlace(request)
        placeTask.addOnSuccessListener { response ->
            isOpenByPlaceObject(response.place)
        }
        placeTask.addOnFailureListener { exception ->
            exception.printStackTrace()
            binding.textViewResponse.text = exception.message
        }
        placeTask.addOnCompleteListener { setLoading(false) }
    }

    /**
     * Check if the place is open at the time specified in the input fields.
     * Requires a Place object that includes Place.Field.ID
     */
    @SuppressLint("SetTextI18n")
    private fun isOpenByPlaceObject(place: Place) {
        clearViews()
        dismissKeyboard(binding.editTextPlaceId)
        setLoading(true)

        val request: IsOpenRequest = try {
            IsOpenRequest.newInstance(place, isOpenCalendar.timeInMillis)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            binding.textViewResponse.text = e.message
            setLoading(false)
            return
        }
        val placeTask: Task<IsOpenResponse> = placesClient.isOpen(request)
        placeTask.addOnSuccessListener { response ->
            binding.textViewResponse.text =
                "Is place open? ${response.isOpen}\nExtra place details: \n${stringify(place)}"
        }
        placeTask.addOnFailureListener {
                exception ->
            exception.printStackTrace()
            binding.textViewResponse.text = exception.message
        }
        placeTask.addOnCompleteListener { setLoading(false) }
    }

    /**
     * Check if the place is open at the time specified in the input fields.
     * Use the Place ID in the input field for the isOpenRequest.
     */
    @SuppressLint("SetTextI18n")
    private fun isOpenByPlaceId() {
        clearViews()
        dismissKeyboard(binding.editTextPlaceId)
        setLoading(true)

        val request: IsOpenRequest = try {
            IsOpenRequest.newInstance(placeId, isOpenCalendar.timeInMillis)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            binding.textViewResponse.text = e.message
            setLoading(false)
            return
        }
        val placeTask: Task<IsOpenResponse> = placesClient.isOpen(request)
        placeTask.addOnSuccessListener { response ->
            binding.textViewResponse.text = "Is place open? " + response.isOpen
        }
        placeTask.addOnFailureListener { exception ->
            exception.printStackTrace()
            binding.textViewResponse.text = exception.message
        }
        placeTask.addOnCompleteListener { setLoading(false) }
    }

    //////////////////////////
    // Helper methods below //
    //////////////////////////
    private fun dismissKeyboard(focusedEditText: EditText) {
        val imm: InputMethodManager =
            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(focusedEditText.windowToken, 0)
    }

    private val placeId: String
        get() = binding.editTextPlaceId.text.toString()

    /**
     * Fetch the fields necessary for an isOpen request, unless user has checked the box to
     * select a custom list of fields. Also fetches name and address for display text.
     */
    private val placeFields: List<Place.Field>
        get() = if (isUseCustomFieldsChecked) {
            fieldSelector.selectedFields
        } else {
            listOf(
                Place.Field.FORMATTED_ADDRESS,
                Place.Field.BUSINESS_STATUS,
                Place.Field.CURRENT_OPENING_HOURS,
                Place.Field.ID,
                Place.Field.DISPLAY_NAME,
                Place.Field.OPENING_HOURS,
                Place.Field.UTC_OFFSET
            )
        }

    private fun setLoading(loading: Boolean) {
        if (loading) {
            binding.progressBarLoading.visibility = View.VISIBLE
        } else {
            binding.progressBarLoading.visibility = View.INVISIBLE
        }
    }

    private fun clearViews() {
        binding.textViewResponse.text = null
    }

    private fun initializeSpinnerAndAddListener() {
        val adapter: ArrayAdapter<String> =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, getAvailableIDs())
        binding.spinnerTimeZones.adapter = adapter
        binding.spinnerTimeZones.setSelection(adapter.getPosition(defaultTimeZoneID))
        binding.spinnerTimeZones.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val timeZone: String = parent!!.getItemAtPosition(position).toString()
                    isOpenCalendar.timeZone = getTimeZone(timeZone)
                    updateIsOpenDate()
                    updateIsOpenTime()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
    }

    private fun addIsOpenDateSelectionListener() {
        val listener: DatePickerDialog.OnDateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, month, day ->
                isOpenCalendar.set(Calendar.YEAR, year)
                isOpenCalendar.set(Calendar.MONTH, month)
                isOpenCalendar.set(Calendar.DAY_OF_MONTH, day)
                updateIsOpenDate()
            }
        binding.editTextIsOpenDate.setOnClickListener {
            DatePickerDialog(
                this@PlaceIsOpenActivity,
                listener,
                isOpenCalendar.get(Calendar.YEAR),
                isOpenCalendar.get(Calendar.MONTH),
                isOpenCalendar.get(Calendar.DAY_OF_MONTH)
            )
                .show()
        }
    }

    private fun updateIsOpenDate() {
        val dateFormat = SimpleDateFormat("MM/dd/yy", Locale.US)
        binding.editTextIsOpenDate.setText(dateFormat.format(isOpenCalendar.timeInMillis))
    }

    private fun addIsOpenTimeSelectionListener() {
        val listener: TimePickerDialog.OnTimeSetListener =
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                isOpenCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                isOpenCalendar.set(Calendar.MINUTE, minute)
                updateIsOpenTime()
            }
        binding.editTextIsOpenTime.setOnClickListener {
            TimePickerDialog(
                this@PlaceIsOpenActivity,
                listener,
                isOpenCalendar.get(Calendar.HOUR_OF_DAY),
                isOpenCalendar.get(Calendar.MINUTE),
                true
            )
                .show()
        }
    }

    private fun updateIsOpenTime() {
        val formattedHour: String =
            String.format(Locale.getDefault(), "%02d", isOpenCalendar.get(Calendar.HOUR_OF_DAY))
        val formattedMinutes: String =
            String.format(Locale.getDefault(), "%02d", isOpenCalendar.get(Calendar.MINUTE))
        binding.editTextIsOpenTime.setText(
            String.format(Locale.getDefault(), "%s:%s", formattedHour, formattedMinutes)
        )
    }

    private val isUseCustomFieldsChecked: Boolean
        get() = binding.checkBoxUseCustomFields.isChecked
}
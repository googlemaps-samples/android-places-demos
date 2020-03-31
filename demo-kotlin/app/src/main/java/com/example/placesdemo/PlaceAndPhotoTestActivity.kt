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

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPhotoResponse
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient

/**
 * Activity for testing [PlacesClient.fetchPlace].
 */
class PlaceAndPhotoTestActivity : AppCompatActivity() {
    private lateinit var placesClient: PlacesClient
    private lateinit var photoView: ImageView
    private lateinit var responseView: TextView
    private lateinit var fieldSelector: FieldSelector

    private var photo: PhotoMetadata? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use whatever theme was set from the MainActivity.
        val theme = intent.getIntExtra(MainActivity.THEME_RES_ID_EXTRA, 0)
        if (theme != 0) {
            setTheme(theme)
        }
        setContentView(R.layout.place_and_photo_test_activity)

        // Retrieve a PlacesClient (previously initialized - see MainActivity)
        placesClient = Places.createClient(this)
        if (savedInstanceState != null && savedInstanceState.containsKey(FETCHED_PHOTO_KEY)) {
            photo = savedInstanceState.getParcelable(FETCHED_PHOTO_KEY)
        }


        // Set up view objects
        responseView = findViewById(R.id.response)
        photoView = findViewById(R.id.photo)
        val fetchPhotoCheckbox = findViewById<CheckBox>(R.id.fetch_photo_checkbox)
        fetchPhotoCheckbox.setOnCheckedChangeListener { _, isChecked: Boolean -> setPhotoSizingEnabled(isChecked) }
        val customPhotoCheckbox = findViewById<CheckBox>(R.id.use_custom_photo_reference)
        customPhotoCheckbox.setOnCheckedChangeListener { _, isChecked: Boolean -> setCustomPhotoReferenceEnabled(isChecked) }
        fieldSelector = FieldSelector(
            findViewById(R.id.use_custom_fields),
            findViewById(R.id.custom_fields_list),
            savedInstanceState
        )

        // Set listeners for programmatic Fetch Place
        findViewById<View>(R.id.fetch_place_and_photo_button).setOnClickListener { fetchPlace() }

        // UI initialization
        setLoading(false)
        setPhotoSizingEnabled(fetchPhotoCheckbox.isChecked)
        setCustomPhotoReferenceEnabled(customPhotoCheckbox.isChecked)
        photo?.let {
            fetchPhoto(it)
        }
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        fieldSelector.onSaveInstanceState(bundle)
        bundle.putParcelable(FETCHED_PHOTO_KEY, photo)
    }

    /**
     * Fetches the [Place] specified via the UI and displays it. May also trigger [ ][.fetchPhoto] if set in the UI.
     */
    private fun fetchPlace() {
        responseView.text = null
        photoView.setImageBitmap(null)
        dismissKeyboard(findViewById(R.id.place_id_field))
        val isFetchPhotoChecked = isFetchPhotoChecked
        val placeFields = placeFields
        val customPhotoReference = customPhotoReference
        if (!validateInputs(isFetchPhotoChecked, placeFields, customPhotoReference)) {
            return
        }
        setLoading(true)
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)
        val placeTask = placesClient.fetchPlace(request)
        placeTask.addOnSuccessListener { response: FetchPlaceResponse ->
            responseView.text = StringUtil.stringify(response, isDisplayRawResultsChecked)
            if (isFetchPhotoChecked) {
                attemptFetchPhoto(response.place)
            }
        }
        placeTask.addOnFailureListener { exception: Exception ->
            exception.printStackTrace()
            responseView.text = exception.message
        }
        placeTask.addOnCompleteListener { setLoading(false) }
    }

    private fun attemptFetchPhoto(place: Place) {
        val photoMetadatas = place.photoMetadatas
        if (photoMetadatas != null && photoMetadatas.isNotEmpty()) {
            fetchPhoto(photoMetadatas[0])
        }
    }

    /**
     * Fetches a Bitmap using the Places API and displays it.
     *
     * @param photoMetadata from a [Place] instance.
     */
    private fun fetchPhoto(photoMetadata: PhotoMetadata) {
        var photoMetadata: PhotoMetadata? = photoMetadata
        photo = photoMetadata
        photoView.setImageBitmap(null)
        setLoading(true)
        val customPhotoReference = customPhotoReference
        if (!TextUtils.isEmpty(customPhotoReference)) {
            photoMetadata = PhotoMetadata.builder(customPhotoReference).build()
        }
        val photoRequestBuilder = FetchPhotoRequest.builder(photoMetadata!!)
        val maxWidth = readIntFromTextView(R.id.photo_max_width)
        if (maxWidth != null) {
            photoRequestBuilder.setMaxWidth(maxWidth)
        }
        val maxHeight = readIntFromTextView(R.id.photo_max_height)
        if (maxHeight != null) {
            photoRequestBuilder.setMaxHeight(maxHeight)
        }
        val photoTask = placesClient.fetchPhoto(photoRequestBuilder.build())
        photoTask.addOnSuccessListener { response: FetchPhotoResponse ->
            val bitmap = response.bitmap
            photoView.setImageBitmap(bitmap)
            StringUtil.prepend(responseView, StringUtil.stringify(bitmap))
        }
        photoTask.addOnFailureListener { exception: Exception ->
            exception.printStackTrace()
            StringUtil.prepend(responseView, "Photo: " + exception.message)
        }
        photoTask.addOnCompleteListener { setLoading(false) }
    }

    //////////////////////////
    // Helper methods below //
    //////////////////////////
    private fun dismissKeyboard(focusedEditText: EditText) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(focusedEditText.windowToken, 0)
    }

    private fun validateInputs(
        isFetchPhotoChecked: Boolean, placeFields: List<Place.Field>, customPhotoReference: String): Boolean {
        if (isFetchPhotoChecked) {
            if (!placeFields.contains(Place.Field.PHOTO_METADATAS)) {
                responseView.text = "'Also fetch photo?' is selected, but PHOTO_METADATAS Place Field is not."
                return false
            }
        } else if (!TextUtils.isEmpty(customPhotoReference)) {
            responseView.text = "Using 'Custom photo reference', but 'Also fetch photo?' is not selected."
            return false
        }
        return true
    }

    private val placeId: String
        get() = findViewById<TextView>(R.id.place_id_field).text.toString()

    private val placeFields: List<Place.Field>
        get() = if (findViewById<CheckBox>(R.id.use_custom_fields).isChecked) {
            fieldSelector.selectedFields
        } else {
            fieldSelector.allFields
        }

    private val isDisplayRawResultsChecked: Boolean
        get() = findViewById<CheckBox>(R.id.display_raw_results).isChecked

    private val isFetchPhotoChecked: Boolean
        get() = findViewById<CheckBox>(R.id.fetch_photo_checkbox).isChecked

    private val customPhotoReference: String
        get() = findViewById<TextView>(R.id.custom_photo_reference).text.toString()

    private fun setPhotoSizingEnabled(enabled: Boolean) {
        setEnabled(R.id.photo_max_width, enabled)
        setEnabled(R.id.photo_max_height, enabled)
    }

    private fun setCustomPhotoReferenceEnabled(enabled: Boolean) {
        setEnabled(R.id.custom_photo_reference, enabled)
    }

    private fun setEnabled(@IdRes resId: Int, enabled: Boolean) {
        val view = findViewById<TextView>(resId)
        view.isEnabled = enabled
        view.text = ""
    }

    private fun readIntFromTextView(@IdRes resId: Int): Int? {
        var intValue: Int? = null
        val view = findViewById<View>(resId)
        if (view is TextView) {
            val contents = view.text
            if (!TextUtils.isEmpty(contents)) {
                try {
                    intValue = contents.toString().toInt()
                } catch (e: NumberFormatException) {
                    showErrorAlert(R.string.error_alert_message_invalid_photo_size)
                }
            }
        }
        return intValue
    }

    private fun showErrorAlert(@StringRes messageResId: Int) {
        AlertDialog.Builder(this)
            .setTitle(R.string.error_alert_title)
            .setMessage(messageResId)
            .show()
    }

    private fun setLoading(loading: Boolean) {
        findViewById<View>(R.id.loading).visibility = if (loading) View.VISIBLE else View.INVISIBLE
    }

    companion object {
        private const val FETCHED_PHOTO_KEY = "photo_image"
    }
}
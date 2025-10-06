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

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.placesdemo.databinding.PlaceDetailsAndPhotosActivityBinding
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.FetchResolvedPhotoUriRequest
import com.google.android.libraries.places.api.net.FetchResolvedPhotoUriResponse
import com.google.android.libraries.places.api.net.PlacesClient

/**
 * Activity to demonstrate [PlacesClient.fetchPlace].
 */
class PlaceDetailsAndPhotosActivity : BaseActivity() {
    private lateinit var placesClient: PlacesClient
    private lateinit var fieldSelector: FieldSelector

    private var photo: PhotoMetadata? = null

    private lateinit var binding: PlaceDetailsAndPhotosActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = PlaceDetailsAndPhotosActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.topBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Retrieve a PlacesClient (previously initialized - see MainActivity)
        placesClient = Places.createClient(this)
        // Restore photo from saved instance state if it exists
        savedInstanceState?.getParcelable<PhotoMetadata>(FETCHED_PHOTO_KEY)?.let { savedPhoto ->
            photo = savedPhoto
        }

        binding.fetchPhotoCheckbox.setOnCheckedChangeListener { _, isChecked: Boolean ->
            setPhotoSizingEnabled(
                isChecked
            )
        }
        binding.useCustomPhotoReference.setOnCheckedChangeListener { _, isChecked: Boolean ->
            setCustomPhotoReferenceEnabled(
                isChecked
            )
        }
        fieldSelector = FieldSelector(
            binding.useCustomFields,
            binding.customFieldsList,
            savedInstanceState
        )

        // Set listeners for programmatic Fetch Place
        binding.fetchPlaceAndPhotoButton.setOnClickListener { fetchPlace() }

        // UI initialization
        setLoading(false)
        setPhotoSizingEnabled(binding.fetchPhotoCheckbox.isChecked)
        setCustomPhotoReferenceEnabled(binding.useCustomPhotoReference.isChecked)
        photo?.let {
            fetchPhoto(it)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        fieldSelector.onSaveInstanceState(outState)
        outState.putParcelable(FETCHED_PHOTO_KEY, photo)
    }

    /**
     * Fetches the [Place] specified via the UI and displays it. May also trigger [ ][.fetchPhoto] if set in the UI.
     */
    private fun fetchPlace() {
        clearViews()

        dismissKeyboard(binding.placeIdField)
        val isFetchPhotoChecked = isFetchPhotoChecked
        val isFetchIconChecked = isFetchIconChecked
        val placeFields = placeFields
        val customPhotoReference = customPhotoReference
        if (!validateInputs(
                isFetchPhotoChecked,
                isFetchIconChecked,
                placeFields,
                customPhotoReference
            )
        ) {
            return
        }
        setLoading(true)
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)
        val placeTask = placesClient.fetchPlace(request)
        placeTask.addOnSuccessListener { response: FetchPlaceResponse ->
            binding.response.text = StringUtil.stringify(response, isDisplayRawResultsChecked)
            if (isFetchPhotoChecked) {
                attemptFetchPhoto(response.place)
            }
            if (isFetchIconChecked) {
                attemptFetchIcon(response.place)
            }
        }
        placeTask.addOnFailureListener { exception: Exception ->
            exception.printStackTrace()
            binding.response.text = exception.message
        }
        placeTask.addOnCompleteListener { setLoading(false) }
    }

    private fun attemptFetchPhoto(place: Place) {
        val photoMetadatas = place.photoMetadatas
        if (photoMetadatas != null && photoMetadatas.isNotEmpty()) {
            fetchPhoto(photoMetadatas[0])
        }
    }

    private fun attemptFetchIcon(place: Place) {
        binding.icon.setImageBitmap(null)
        place.iconBackgroundColor?.let { binding.icon.setBackgroundColor(it) }
        val url = place.iconMaskUrl
        Glide.with(this).load(url).into(binding.icon)
    }

    /**
     * Fetches a Bitmap using the Places API and displays it.
     *
     * @param photoMetadata from a [Place] instance.
     */
    private fun fetchPhoto(photoMetadata: PhotoMetadata) {
        var localPhotoMetadata: PhotoMetadata? = photoMetadata
        photo = localPhotoMetadata
        binding.photo.setImageBitmap(null)
        setLoading(true)
        val customPhotoReference = customPhotoReference
        if (!TextUtils.isEmpty(customPhotoReference)) {
            localPhotoMetadata = PhotoMetadata.builder(customPhotoReference).build()
        }
        val photoRequestBuilder = FetchResolvedPhotoUriRequest.builder(localPhotoMetadata!!)
        val maxWidth = readIntFromTextView(binding.photoMaxWidth)
        if (maxWidth != null) {
            photoRequestBuilder.maxWidth = maxWidth
        }
        val maxHeight = readIntFromTextView(binding.photoMaxHeight)
        if (maxHeight != null) {
            photoRequestBuilder.maxHeight = maxHeight
        }
        val photoTask = placesClient.fetchResolvedPhotoUri(photoRequestBuilder.build())
        photoTask.addOnSuccessListener { response: FetchResolvedPhotoUriResponse ->
            val uri = response.uri
            if (uri != null) {
                Glide.with(binding.photo.context)
                    .load(uri)
                    .into(binding.photo)

                StringUtil.prepend(binding.photoMetadata, StringUtil.stringify(uri))
            } else {
                StringUtil.prepend(binding.photoMetadata, "No photo available")
            }
        }
        photoTask.addOnFailureListener { exception: Exception ->
            exception.printStackTrace()
            StringUtil.prepend(binding.response, "Photo: " + exception.message)
        }
        photoTask.addOnCompleteListener { setLoading(false) }
    }

    //////////////////////////
    // Helper methods below //
    //////////////////////////
    private fun dismissKeyboard(focusedEditText: EditText) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(focusedEditText.windowToken, 0)
    }

    private fun validateInputs(
        isFetchPhotoChecked: Boolean,
        isFetchIconChecked: Boolean,
        placeFields: List<Place.Field>,
        customPhotoReference: String
    ): Boolean {
        if (isFetchPhotoChecked) {
            if (!placeFields.contains(Place.Field.PHOTO_METADATAS)) {
                binding.response.setText(R.string.fetch_photo_selected_but_no_metadata)
                return false
            }
        } else if (!TextUtils.isEmpty(customPhotoReference)) {
            binding.response.setText(R.string.custom_photo_reference_but_not_fetch_photo)
            return false
        }
        if (isFetchIconChecked && !placeFields.contains(Place.Field.ICON_MASK_URL)) {
            binding.response.setText(R.string.fetch_icon_missing_fields_warning)
            return false
        }
        return true
    }

    private val placeId: String
        get() = binding.placeIdField.text.toString()

    private val placeFields: List<Place.Field>
        get() = if (findViewById<CheckBox>(R.id.use_custom_fields).isChecked) {
            fieldSelector.selectedFields
        } else {
            fieldSelector.allFields
        }

    private val isDisplayRawResultsChecked: Boolean
        get() = binding.displayRawResults.isChecked

    private val isFetchPhotoChecked: Boolean
        get() = binding.fetchPhotoCheckbox.isChecked

    private val isFetchIconChecked: Boolean
        get() = binding.fetchIconCheckbox.isChecked

    private val customPhotoReference: String
        get() = binding.customPhotoReference.text.toString()

    private fun setPhotoSizingEnabled(enabled: Boolean) {
        setEnabled(binding.photoMaxWidth, enabled)
        setEnabled(binding.photoMaxHeight, enabled)
    }

    private fun setCustomPhotoReferenceEnabled(enabled: Boolean) {
        setEnabled(binding.customPhotoReference, enabled)
    }

    private fun setEnabled(textView: TextView, enabled: Boolean) {
        textView.isEnabled = enabled
        textView.text = ""
    }

    private fun readIntFromTextView(textView: TextView): Int? {
        var intValue: Int? = null
        val contents = textView.text
        if (!TextUtils.isEmpty(contents)) {
            try {
                intValue = contents.toString().toInt()
            } catch (e: NumberFormatException) {
                showErrorAlert(R.string.error_alert_message_invalid_photo_size)
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

    private fun clearViews() {
        binding.response.text = null
        binding.photo.setImageBitmap(null)
        binding.photoMetadata.text = null
        binding.icon.setImageBitmap(null)
    }

    companion object {
        private const val FETCHED_PHOTO_KEY = "photo_image"
    }
}
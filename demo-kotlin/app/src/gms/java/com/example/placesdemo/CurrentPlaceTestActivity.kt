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

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.ACCESS_WIFI_STATE
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.placesdemo.StringUtil.stringify
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
            Place.Field.WEBSITE_URI
        )
        fieldSelector = FieldSelector(
            findViewById(R.id.use_custom_fields),
            findViewById(R.id.custom_fields_list),
            savedInstanceState,
            placeFields,
        )
        responseView = findViewById(R.id.response)
        setLoading(false)

        // Set listeners for programmatic Find Current Place
        findViewById<Button>(R.id.find_current_place_button)
            .setOnClickListener { findCurrentPlace() }
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        fieldSelector.onSaveInstanceState(bundle)
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            )
            return
        }

        if (isLocationPermissionGranted(permissions, grantResults)) {
            findCurrentPlaceWithPermissions()
        }
    }

    private fun isLocationPermissionGranted(
        permissions: Array<String>,
        grantResults: IntArray
    ): Boolean =
        permissions.toList().zip(grantResults.toList())
            .firstOrNull { (permission, grantResult) ->
                grantResult == PackageManager.PERMISSION_GRANTED && (permission == ACCESS_FINE_LOCATION || permission == ACCESS_COARSE_LOCATION)
            } != null

    /**
     * Fetches a list of [PlaceLikelihood] instances that represent the Places the user is
     * most likely to be at currently.
     */
    @SuppressLint("MissingPermission")
    private fun findCurrentPlace() {
        if (ContextCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                ACCESS_COARSE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(
                this,
                "Either ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION permission is required.",
                Toast.LENGTH_SHORT
            ).show()
        }

        // 1. Check if either ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION is granted. If so,
        // proceed with finding the current place.
        if (hasOnePermissionGranted(
                ACCESS_FINE_LOCATION,
                ACCESS_COARSE_LOCATION
            )
        ) {
            Log.d(TAG, "Location permission granted. Getting current place.")
            findCurrentPlaceWithPermissions()
            return
        }

        // 2. If either permission is not granted, check if a permission rationale dialog must be
        // shown
        if (shouldShowPermissionRationale(
                ACCESS_FINE_LOCATION,
                ACCESS_COARSE_LOCATION
            )
        ) {
            Log.d(TAG, "Showing permission rationale dialog")
            RationaleDialog.newInstance(
                PERMISSION_REQUEST_CODE, true
            ).show(supportFragmentManager, "dialog")
            return
        }

        // 3. Otherwise, request permission
        Log.d(
            TAG,
            "No location permission granted. Request permission from the user."
        )
        ActivityCompat
            .requestPermissions(
                this,
                arrayOf(
                    ACCESS_FINE_LOCATION,
                    ACCESS_COARSE_LOCATION
                ),
                PERMISSION_REQUEST_CODE
            )
    }

    /**
     * Fetches a list of [PlaceLikelihood] instances that represent the Places the user is
     * most likely to be at currently.
     */
    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    private fun findCurrentPlaceWithPermissions() {
        setLoading(true)
        val currentPlaceRequest = FindCurrentPlaceRequest.newInstance(
            placeFields
        )

        // Safe to suppress permission for ACCESS_WIFI_STATE since this is added in the manifest
        // file by the Places SDK
        @SuppressLint("MissingPermission") val currentPlaceTask =
            placesClient.findCurrentPlace(currentPlaceRequest)
        currentPlaceTask.addOnSuccessListener { response: FindCurrentPlaceResponse? ->
            response?.let {
                responseView.text = stringify(
                    it, isDisplayRawResultsChecked
                )
            }
        }
        currentPlaceTask.addOnFailureListener { exception: Exception ->
            exception.printStackTrace()
            responseView.text = exception.message
        }
        currentPlaceTask.addOnCompleteListener {
            setLoading(false)
        }
    }

    //////////////////////////
    // Helper methods below //
    //////////////////////////
    private val placeFields: List<Place.Field>
        get() = if (findViewById<CheckBox>(R.id.use_custom_fields).isChecked) {
            fieldSelector.selectedFields
        } else {
            fieldSelector.allFields
        }

    private fun shouldShowPermissionRationale(vararg permissions: String): Boolean =
        permissions.any {
            ActivityCompat.shouldShowRequestPermissionRationale(this, it)
        }

    private fun hasOnePermissionGranted(vararg permissions: String): Boolean =
        permissions.any {
            ContextCompat.checkSelfPermission(
                this,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }

    private val isDisplayRawResultsChecked: Boolean
        get() = findViewById<CheckBox>(R.id.display_raw_results).isChecked

    private fun setLoading(loading: Boolean) {
        findViewById<View>(R.id.loading).visibility =
            if (loading) View.VISIBLE else View.INVISIBLE
    }

    /**
     * A dialog that explains the use of the location permission and requests the necessary
     * permission.
     *
     *
     * The activity should implement [ActivityCompat.OnRequestPermissionsResultCallback]
     * to handle permit or denial of this permission request.
     */
    class RationaleDialog : DialogFragment() {
        private var finishActivity = false
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val arguments = arguments
            val requestCode = arguments!!.getInt(
                ARGUMENT_PERMISSION_REQUEST_CODE
            )
            finishActivity = arguments.getBoolean(ARGUMENT_FINISH_ACTIVITY)
            return AlertDialog.Builder(requireActivity()).apply {
                setMessage(R.string.permission_rationale_location)
                setPositiveButton(android.R.string.ok) { dialog: DialogInterface?, which: Int ->
                    // After click on Ok, request the permission.
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(
                            ACCESS_FINE_LOCATION,
                            ACCESS_COARSE_LOCATION
                        ),
                        requestCode
                    )
                    // Do not finish the Activity while requesting permission.
                    finishActivity = false
                }
                setNegativeButton(android.R.string.cancel, null)
            }.create()
        }

        override fun onDismiss(dialog: DialogInterface) {
            super.onDismiss(dialog)
            if (finishActivity) {
                Toast.makeText(
                    requireContext(),
                    R.string.permission_required_toast,
                    Toast.LENGTH_SHORT
                ).show()
                requireActivity().finish()
            }
        }

        companion object {
            private const val ARGUMENT_PERMISSION_REQUEST_CODE = "requestCode"
            private const val ARGUMENT_FINISH_ACTIVITY = "finish"

            /**
             * Creates a new instance of a dialog displaying the rationale for the use of the location
             * permission.
             *
             *
             * The permission is requested after clicking 'ok'.
             *
             * @param requestCode Id of the request that is used to request the permission. It is
             * returned to the [ActivityCompat.OnRequestPermissionsResultCallback].
             * @param finishActivity Whether the calling Activity should be finished if the dialog is
             * cancelled.
             */
            fun newInstance(
                requestCode: Int,
                finishActivity: Boolean
            ): RationaleDialog {
                val dialog = RationaleDialog()
                dialog.arguments = Bundle().apply {
                    putInt(ARGUMENT_PERMISSION_REQUEST_CODE, requestCode)
                    putBoolean(ARGUMENT_FINISH_ACTIVITY, finishActivity)
                }
                return dialog
            }
        }
    }

    companion object {
        private val TAG = CurrentPlaceTestActivity::class.java.simpleName
        private const val PERMISSION_REQUEST_CODE = 9
    }
}

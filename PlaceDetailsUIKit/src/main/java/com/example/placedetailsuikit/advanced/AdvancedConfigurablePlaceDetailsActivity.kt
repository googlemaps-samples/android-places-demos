// Copyright 2026 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.placedetailsuikit.advanced

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import com.example.placedetailsuikit.BuildConfig
import com.example.placedetailsuikit.R
import com.example.placedetailsuikit.databinding.ActivityAdvancedConfigurableMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AdvancedPlaceDetailsCompactFragment
import com.google.android.libraries.places.widget.PlaceLoadListener
import com.google.android.libraries.places.widget.model.CornerPlaceAction
import com.google.android.libraries.places.widget.model.Orientation
import com.google.android.libraries.places.widget.model.PlaceAction
import com.google.android.libraries.places.widget.model.PlaceActionProvider
import com.google.android.libraries.places.widget.model.SearchMediaOptions

private const val TAG = "AdvancedPlacesUiKit"

class AdvancedMainViewModel : ViewModel() {
    var selectedPlaceId: String? = null
}

/**
 * Demonstrates Places SDK 5.3.0 Advanced Place Details UI Kit widgets (`AdvancedPlaceDetailsCompactFragment`).
 * Shows configuring custom action buttons via `PlaceActionProvider` (`PlaceAction.CALL`, `OPEN_WEBSITE`,
 * `OPEN_DIRECTIONS`, `OPEN_IN_MAPS`) and applying `SearchMediaOptions`.
 */
class AdvancedConfigurablePlaceDetailsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnPoiClickListener {

    private lateinit var binding: ActivityAdvancedConfigurableMapBinding
    private var googleMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private val viewModel: AdvancedMainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdvancedConfigurableMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                    fetchLastLocation()
                } else {
                    handleLocationError()
                }
            }

        binding.dismissButton.setOnClickListener {
            dismissPlaceDetails()
        }

        val apiKey = BuildConfig.PLACES_API_KEY
        if (apiKey.isEmpty() || apiKey == "YOUR_API_KEY") {
            Toast.makeText(this, "Add your own API_KEY in local.properties", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        if (viewModel.selectedPlaceId != null) {
            viewModel.selectedPlaceId?.let { placeId ->
                showPlaceDetailsFragment(placeId)
            }
        }

        binding.configureButton.setOnClickListener {
            Toast.makeText(
                this,
                "Places 5.3.0: Using AdvancedPlaceDetailsCompactFragment with custom PlaceActionProvider & SearchMediaOptions!",
                Toast.LENGTH_LONG
            ).show()
        }

        binding.myLocationButton.setOnClickListener {
            fetchLastLocation()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.setOnPoiClickListener(this)

        if (isLocationPermissionGranted()) {
            fetchLastLocation()
        } else {
            requestLocationPermissions()
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissions() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun handleLocationError() {
        Toast.makeText(this, "Showing default location (Sydney).", Toast.LENGTH_LONG).show()
        moveToSydney()
    }

    @SuppressLint("MissingPermission")
    private fun fetchLastLocation() {
        if (isLocationPermissionGranted()) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val latLng = LatLng(location.latitude, location.longitude)
                        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    } else {
                        handleLocationError()
                    }
                }
                .addOnFailureListener {
                    handleLocationError()
                }
        } else {
            requestLocationPermissions()
        }
    }

    private fun moveToSydney() {
        val sydney = LatLng(-33.8688, 151.2093)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 13f))
    }

    override fun onPoiClick(poi: PointOfInterest) {
        val placeId = poi.placeId
        viewModel.selectedPlaceId = placeId
        showPlaceDetailsFragment(placeId)
    }

    private fun showPlaceDetailsFragment(placeId: String) {
        binding.placeDetailsWrapper.visibility = View.VISIBLE
        binding.dismissButton.visibility = View.GONE
        binding.placeDetailsContainer.visibility = View.GONE
        binding.loadingIndicatorConfigurable.visibility = View.VISIBLE

        val orientation = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Orientation.HORIZONTAL
        } else {
            Orientation.VERTICAL
        }

        val fragment = AdvancedPlaceDetailsCompactFragment.newInstance(
            AdvancedPlaceDetailsCompactFragment.ALL_CONTENT,
            orientation,
            R.style.CustomizedPlaceDetailsTheme,
        ).apply {
            setPlaceLoadListener(object : PlaceLoadListener {
                override fun onSuccess(place: Place) {
                    binding.loadingIndicatorConfigurable.visibility = View.GONE
                    binding.placeDetailsContainer.visibility = View.VISIBLE
                    binding.dismissButton.visibility = View.VISIBLE
                }

                override fun onFailure(e: Exception) {
                    dismissPlaceDetails()
                    Toast.makeText(this@AdvancedConfigurablePlaceDetailsActivity, "Failed to load advanced place details.", Toast.LENGTH_SHORT).show()
                }
            })
            setPlaceActionProvider(object : PlaceActionProvider {
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
            })
            applySearchMediaOptions(
                SearchMediaOptions.builder()
                    .setQuery("menu")
                    .setRankPreference(SearchMediaOptions.RankPreference.MOST_RELEVANT)
                    .build()
            )
        }

        supportFragmentManager
            .beginTransaction()
            .replace(binding.placeDetailsContainer.id, fragment)
            .commitNow()

        binding.root.post {
            fragment.loadWithPlaceId(placeId)
        }
    }

    private fun dismissPlaceDetails() {
        binding.placeDetailsWrapper.visibility = View.GONE
        viewModel.selectedPlaceId = null
    }

    override fun onDestroy() {
        super.onDestroy()
        googleMap = null
    }
}

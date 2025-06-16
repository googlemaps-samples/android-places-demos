package com.example.placedetailsuikit

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.placedetailsuikit.databinding.ActivityMainBinding
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
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.PlaceDetailsCompactFragment
import com.google.android.libraries.places.widget.PlaceLoadListener
import com.google.android.libraries.places.widget.model.Orientation

private const val TAG = "PlacesUiKit"

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnPoiClickListener {
    private lateinit var binding: ActivityMainBinding
    private var googleMap: GoogleMap? = null
    private val orientation: Orientation = Orientation.HORIZONTAL
    private lateinit var placesClient: PlacesClient
    private var placeDetailsFragment: PlaceDetailsCompactFragment? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register the permissions callback, which handles the user's response to the
        // system permissions dialog. Save the return value, an instance of
        // ActivityResultLauncher. You can use either a val, as shown in this snippet,
        // or a lateinit var in your onAttach() or onCreate() method.
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                    // Location permission has been granted.
                    Log.d(TAG, "Location permission granted by user.")
                    fetchLastLocation()
                } else {
                    // Location permission has been denied.
                    Log.d(TAG, "Location permission denied by user.")
                    Toast.makeText(
                        this,
                        "Location permission denied. Showing default location.",
                        Toast.LENGTH_LONG
                    ).show()
                    moveToSydney()
                }
            }

        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // --- Initialize Places SDK ---
        val apiKey = BuildConfig.PLACES_API_KEY
        if (apiKey.isEmpty() || apiKey == "YOUR_API_KEY") {
            Log.e(TAG, "No api key")
            Toast.makeText(
                this,
                "Add your own API_KEY in local.properties",
                Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }
        Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)
        placesClient = Places.createClient(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // -----------------------------

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap) {
        Log.d(TAG, "Map is ready")
        googleMap = map
        googleMap?.setOnPoiClickListener(this)

        if (isLocationPermissionGranted()) {
            fetchLastLocation()
        } else {
            requestLocationPermissions()
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissions() {
        Log.d(TAG, "Requesting location permissions.")
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun fetchLastLocation() {
        if (isLocationPermissionGranted()) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        // A last known location is available, move the camera there.
                        val userLocation = LatLng(location.latitude, location.longitude)
                        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 13f))
                        Log.d(TAG, "Moved to user's last known location.")
                    } else {
                        // Last known location is null, default to Sydney.
                        Log.d(TAG, "Last known location is null. Falling back to Sydney.")
                        moveToSydney()
                    }
                }
                .addOnFailureListener {
                    // An error occurred, default to Sydney.
                    Log.e(TAG, "Failed to get location.", it)
                    moveToSydney()
                }
        }
    }


    private fun moveToSydney() {
        val sydney = LatLng(-33.8688, 151.2093)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 13f))
        Log.d(TAG, "Moved to Sydney")
    }

    override fun onPoiClick(poi: PointOfInterest) {
        val placeId = poi.placeId
        Log.d(TAG, "Place ID: $placeId")
        showPlaceDetailsFragment(placeId)
    }

    private fun showPlaceDetailsFragment(placeId: String) {
        Log.d(TAG, "Place ID: $placeId")

        // Show loading indicator and hide the old card content.
        binding.placeDetailsContainer.visibility = View.GONE
        binding.loadingIndicator.visibility = View.VISIBLE


        val fragment = PlaceDetailsCompactFragment.newInstance(
            content = PlaceDetailsCompactFragment.ALL_CONTENT,
            orientation = orientation,
        ).apply {
            setPlaceLoadListener(object : PlaceLoadListener {
                override fun onSuccess(place: Place) {
                    Log.d(TAG, "Place loaded: $place")
                    // Hide loading and show the new card content.
                    binding.loadingIndicator.visibility = View.GONE
                    binding.placeDetailsContainer.visibility = View.VISIBLE
                }

                override fun onFailure(e: Exception) {
                    Log.e(TAG, "Place failed to load", e)
                    binding.loadingIndicator.visibility = View.GONE
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to load place details.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }

        supportFragmentManager
            .beginTransaction()
            .replace(binding.placeDetailsContainer.id, fragment)
            .commitNow()

        // Load the fragment with a Place ID.
        fragment.loadWithPlaceId(placeId)

        placeDetailsFragment = fragment
    }
}

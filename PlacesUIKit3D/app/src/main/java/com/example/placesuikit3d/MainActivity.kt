package com.example.placesuikit3d

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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import com.example.placesuikit3d.utils.toValidCamera
import com.example.placesuikit3d.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.PlaceDetailsCompactFragment
import com.google.android.libraries.places.widget.PlaceLoadListener
import com.google.android.libraries.places.widget.model.Orientation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    var selectedPlaceId: String? = null
}

class MainActivity : AppCompatActivity(), OnMap3DViewReadyCallback {
    private val TAG = this::class.java.simpleName
    private lateinit var binding: ActivityMainBinding
    private var googleMap3D: GoogleMap3D? = null
    private var placeDetailsFragment: PlaceDetailsCompactFragment? = null
    private val orientation: Orientation = Orientation.HORIZONTAL

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private val viewModel: MainViewModel by viewModels()

    private val initialCamera: Camera = camera {
        center = latLngAltitude {
            latitude = 47.62053235109065
            longitude = -122.34927268590577
            altitude = 56.0
        }
        heading = 152.0
        tilt = 60.0
        range = 3000.0
    }.toValidCamera()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                    Log.d(TAG, "Location permission granted by user.")
                    fetchLastLocation()
                } else {
                    Log.d(TAG, "Location permission denied by user.")
                    Toast.makeText(
                        this,
                        "Location permission denied. Showing default location.",
                        Toast.LENGTH_SHORT
                    ).show()
                    moveToDefaultLocation()
                }
            }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.map3dView.onCreate(savedInstanceState)
        binding.map3dView.getMap3DViewAsync(this)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        val isLightTheme = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_NO
        insetsController.isAppearanceLightStatusBars = isLightTheme

        val contentView = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(contentView) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        binding.myLocationButton.setOnClickListener {
            fetchLastLocation()
        }

        binding.dismissButton.setOnClickListener {
            dismissPlaceDetails()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.map3dView.onCreate(savedInstanceState)
        binding.map3dView.getMap3DViewAsync(this)

        if (viewModel.selectedPlaceId != null) {
            viewModel.selectedPlaceId?.let { placeId ->
                Log.d(TAG, "Restoring PlaceDetailsFragment for place ID: $placeId")
                showPlaceDetailsFragment(placeId)
            }
        }
    }

    override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
        this.googleMap3D = googleMap3D
        googleMap3D.setMapMode(Map3DMode.HYBRID)
        googleMap3D.setCameraRestriction(null)
        googleMap3D.setCamera(initialCamera)

        googleMap3D.setMap3DClickListener { location: LatLngAltitude, placeId: String? ->
            Log.d(
                "MainActivity",
                "onMap3DClick: ${location.latitude}, ${location.longitude}, ${location.altitude}, $placeId",
            )
            if (placeId != null) {
                viewModel.selectedPlaceId = placeId
                showPlaceDetailsFragment(placeId)
            } else {
                dismissPlaceDetails()
            }
        }

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

    @SuppressLint("MissingPermission")
    private fun fetchLastLocation() {
        if (isLocationPermissionGranted()) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val userLocation = latLngAltitude {
                            latitude = location.latitude
                            longitude = location.longitude
                            altitude = 0.0 // Altitude from location provider is not always reliable
                        }
                        googleMap3D?.flyCameraTo(
                            flyToOptions {
                                endCamera = camera {
                                    center = userLocation
                                    range = 5000.0
                                    tilt = 60.0
                                }.toValidCamera()
                                durationInMillis = 3000
                            }
                        )
                        Log.d(TAG, "Moved to user's last known location.")
                    } else {
                        Log.d(TAG, "Last known location is null. Falling back to default.")
                        moveToDefaultLocation()
                    }
                }
                .addOnFailureListener {
                    Log.e(TAG, "Failed to get location.", it)
                    moveToDefaultLocation()
                }
        }
    }

    private fun moveToDefaultLocation() {
        googleMap3D?.flyCameraTo(
            flyToOptions {
                endCamera = initialCamera
                durationInMillis = 3000
            }
        )
        googleMap3D?.setCamera(
            initialCamera
        )
        Log.d(TAG, "Moved to default location")
    }


    private fun showPlaceDetailsFragment(placeId: String) {
        Log.d(TAG, "Showing PlaceDetailsFragment for place ID: $placeId")

        // Launch in the main dispatcher using the global scope.
        CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            binding.placeDetailsWrapper.visibility = View.VISIBLE
            binding.loadingContainer.visibility = View.VISIBLE
            binding.dismissButton.visibility = View.GONE
            binding.placeDetailsContainer.visibility = View.GONE

            val orientation =
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    Orientation.HORIZONTAL
                } else {
                    Orientation.VERTICAL
                }

            placeDetailsFragment = PlaceDetailsCompactFragment.newInstance(
                PlaceDetailsCompactFragment.ALL_CONTENT,
                orientation,
                R.style.CustomizedPlaceDetailsTheme,
            ).apply {
                setPlaceLoadListener(object : PlaceLoadListener {
                    override fun onSuccess(place: Place) {
                        Log.d(TAG, "Place loaded: ${place.id}")
                        binding.loadingContainer.visibility = View.GONE
                        binding.placeDetailsContainer.visibility = View.VISIBLE
                        binding.dismissButton.visibility = View.VISIBLE
                    }

                    override fun onFailure(e: Exception) {
                        Log.e(TAG, "Place failed to load", e)
                        binding.loadingContainer.visibility = View.GONE
                        dismissPlaceDetails()
                        Toast.makeText(this@MainActivity, "Failed to load place details.", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            supportFragmentManager
                .beginTransaction()
                .replace(binding.placeDetailsContainer.id, placeDetailsFragment!!)
                .commitNow()

            placeDetailsFragment?.loadWithPlaceId(placeId)
        }
    }

    private fun dismissPlaceDetails() {
        binding.placeDetailsWrapper.visibility = View.GONE
        viewModel.selectedPlaceId = null
    }

    override fun onError(error: Exception) {
        Log.e(TAG, "Error loading map", error)
        super.onError(error)
    }

    override fun onResume() {
        super.onResume()
        binding.map3dView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.map3dView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.map3dView.onDestroy()
    }

    @Deprecated("Deprecated in Java")
    override fun onLowMemory() {
        super.onLowMemory()
        binding.map3dView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.map3dView.onSaveInstanceState(outState)
    }
}

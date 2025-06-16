package com.example.placedetailsuikit

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.placedetailsuikit.databinding.ActivityMainBinding
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
import com.google.android.libraries.places.widget.model.Orientation
import com.google.android.libraries.places.widget.PlaceDetailsCompactFragment.Companion.ALL_CONTENT
import com.google.android.libraries.places.widget.PlaceLoadListener

private const val TAG = "PlacesUiKit"

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnPoiClickListener {
    private lateinit var binding: ActivityMainBinding
    private var googleMap: GoogleMap? = null
    private val orientation: Orientation = Orientation.HORIZONTAL
    private lateinit var placesClient: PlacesClient
    private var placeDetailsFragment: PlaceDetailsCompactFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle insets
//        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        // --- Initialize Places SDK ---
        val apiKey = BuildConfig.PLACES_API_KEY
        if (apiKey.isEmpty() || apiKey == "YOUR_API_KEY") {
            Log.e(TAG, "No api key")
            Toast.makeText(
                this,
                "Add your own API_KEY in maps-api-key.xml",
                Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }
        Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)
        placesClient = Places.createClient(this)
        // -----------------------------

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        val sydney = LatLng(-33.8688, 151.2093)
        val zoomLevel = 13f
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, zoomLevel))
        googleMap?.setOnPoiClickListener(this)
    }

    override fun onPoiClick(poi: PointOfInterest) {
        val placeId = poi.placeId
        Log.d(TAG, "Place ID: $placeId")

        binding.placeDetailsContainer.visibility = View.GONE
        binding.loadingIndicator.visibility = View.VISIBLE

        showPlaceDetailsFragment(placeId)
    }

    private fun showPlaceDetailsFragment(placeId: String) {
        Log.d(TAG, "Place ID: $placeId")

        // listOf(Content.ADDRESS, Content.TYPE, Content.RATING, Content.ACCESSIBLE_ENTRANCE_ICON),

        val fragment = PlaceDetailsCompactFragment.newInstance(
            content = ALL_CONTENT,
            orientation = orientation,
//            R.style.CustomizedPlaceDetailsTheme
        ).apply {
            setPlaceLoadListener(object : PlaceLoadListener {
                override fun onSuccess(place: Place) {
                    Log.d(TAG, "Place loaded: $place")
                    binding.loadingIndicator.visibility = View.GONE
                    binding.placeDetailsContainer.visibility = View.VISIBLE
                }
                override fun onFailure(e: Exception) {
                    Log.e(TAG, "Place failed to load", e)
                    binding.loadingIndicator.visibility = View.GONE
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to load place details.",
                        Toast.LENGTH_SHORT)
                        .show()
                }
            })
        }

        supportFragmentManager
            .beginTransaction()
            .replace(binding.placeDetailsContainer.id, fragment)
            .commitNow()

        // Load the fragment with a Place ID.
        fragment.loadWithPlaceId(placeId)
        // Load the fragment with a resource name.
        // fragment.loadWithResourceName(resourceName)

        placeDetailsFragment = fragment
    }
}
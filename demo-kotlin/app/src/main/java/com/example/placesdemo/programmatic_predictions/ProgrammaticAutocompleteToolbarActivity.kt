// Copyright 2020 Google LLC
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

package com.example.placesdemo.programmatic_predictions

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.ViewAnimator
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.placesdemo.MainActivity
import com.example.placesdemo.R
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient

/**
 * An Activity that demonstrates programmatic as-you-type place predictions.
 */
class ProgrammaticAutocompleteToolbarActivity : AppCompatActivity() {

    private val handler = Handler()
    private val adapter = PlacePredictionAdapter()
    private lateinit var placesClient: PlacesClient
    private lateinit var viewAnimator: ViewAnimator
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use whatever theme was set from the MainActivity - some of these colors (e.g primary color)
        // will get picked up by the AutocompleteActivity.
        val theme = intent.getIntExtra(MainActivity.THEME_RES_ID_EXTRA, 0)
        if (theme != 0) {
            setTheme(theme)
        }
        setContentView(R.layout.activity_programmatic_autocomplete)
        setSupportActionBar(findViewById(R.id.toolbar))

        // Initialize members
        progressBar = findViewById(R.id.progress_bar)
        viewAnimator = findViewById(R.id.view_animator)
        placesClient = Places.createClient(this)
        initRecyclerView()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        val searchView = menu.findItem(R.id.search).actionView as SearchView
        initSearchView(searchView)
        return super.onCreateOptionsMenu(menu)
    }

    private fun initSearchView(searchView: SearchView) {
        searchView.queryHint = getString(R.string.search_a_place)
        searchView.isIconifiedByDefault = false
        searchView.isFocusable = true
        searchView.isIconified = false
        searchView.requestFocusFromTouch()
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                progressBar.isIndeterminate = true

                // Cancel any previous place prediction requests
                handler.removeCallbacksAndMessages(null)

                // Start a new place prediction request in 300 ms
                handler.postDelayed({ getPlacePredictions(newText) }, 300)
                return true
            }
        })
    }

    private fun initRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        recyclerView .addItemDecoration(DividerItemDecoration(this, layoutManager.orientation))
    }

    /**
     * This method demonstrates the programmatic approach to getting place predictions
     *
     * @param query the plus code query string (e.g. "MC2W+RG K")
     */
    private fun getPlacePredictions(query: String) {
        // Create a new programmatic Place Autocomplete request in Places SDK for Android
        val newRequest = FindAutocompletePredictionsRequest
            .builder()
            .setTypeFilter(TypeFilter.ESTABLISHMENT)
            .setQuery(query)
            .setCountries("US")
            .build()

        // Perform autocomplete predictions request
        placesClient.findAutocompletePredictions(newRequest).addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
            val predictions = response.autocompletePredictions
            adapter.setPredictions(predictions)
            progressBar.isIndeterminate = false
            viewAnimator.displayedChild = if (predictions.isEmpty()) 0 else 1
        }.addOnFailureListener { exception: Exception? ->
            progressBar.isIndeterminate = false
            if (exception is ApiException) {
                Log.e(TAG, "Place not found: " + exception.statusCode)
            }
        }
    }

    companion object {
        private val TAG = ProgrammaticAutocompleteToolbarActivity::class.java.simpleName
    }
}
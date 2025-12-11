// Copyright 2025 Google LLC
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

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.places.R
import com.google.places.databinding.ActivityMainBinding
import com.google.places.databinding.ListItemActivityBinding

class KotlinMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "$title (Kotlin)"

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val activities = listOf(
            ActivityInfo(
                "Current Place",
                getString(R.string.current_place_description),
                CurrentPlaceActivity::class.java
            ),
            ActivityInfo(
                "Place Autocomplete",
                getString(R.string.place_autocomplete_description),
                PlaceAutocompleteActivity::class.java
            ),
            ActivityInfo(
                "Place Details",
                getString(R.string.place_details_description),
                PlaceDetailsActivity::class.java
            ),
            ActivityInfo(
                "Place Photos",
                getString(R.string.place_photos_description),
                PlacePhotosActivity::class.java
            ),
            ActivityInfo(
                "Places Icon",
                getString(R.string.places_icon_description),
                PlacesIconActivity::class.java
            ),
            ActivityInfo(
                "Place Is Open",
                getString(R.string.place_is_open_description),
                PlaceIsOpenActivity::class.java
            )
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = ActivitiesAdapter(activities)
    }

    private data class ActivityInfo(
        val name: String,
        val description: String,
        val activityClass: Class<out AppCompatActivity>
    )

    private class ActivitiesAdapter(private val activities: List<ActivityInfo>) :
        RecyclerView.Adapter<ActivitiesAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ListItemActivityBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val activityInfo = activities[position]
            holder.binding.activityName.text = activityInfo.name
            holder.binding.activityDescription.text = activityInfo.description
            holder.itemView.setOnClickListener {
                val intent = Intent(holder.itemView.context, activityInfo.activityClass)
                holder.itemView.context.startActivity(intent)
            }
        }

        override fun getItemCount(): Int = activities.size

        class ViewHolder(val binding: ListItemActivityBinding) : RecyclerView.ViewHolder(binding.root)
    }
}

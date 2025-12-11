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

package com.google.places;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.places.databinding.ActivityMainBinding;
import com.google.places.databinding.ListItemActivityBinding;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class JavaMainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display to draw behind the system bars for a more immersive experience.
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getTitle() + " (Java)");

        // Apply window insets to the root view.
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        List<ActivityInfo> activities = Arrays.asList(
            new ActivityInfo(
                "Current Place",
                getString(R.string.current_place_description),
                CurrentPlaceActivity.class
            ),
            new ActivityInfo(
                "Place Autocomplete",
                getString(R.string.place_autocomplete_description),
                PlaceAutocompleteActivity.class
            ),
            new ActivityInfo(
                "Place Details",
                getString(R.string.place_details_description),
                PlaceDetailsActivity.class
            ),
            new ActivityInfo(
                "Place Photos",
                getString(R.string.place_photos_description),
                PlacePhotosActivity.class
            ),
            new ActivityInfo(
                "Places Icon",
                getString(R.string.places_icon_description),
                PlacesIconActivity.class
            ),
            new ActivityInfo(
                "Place Is Open",
                getString(R.string.place_is_open_description),
                PlaceIsOpenActivity.class
            )
        );

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(new ActivitiesAdapter(activities));
    }

    private static class ActivityInfo {
        private final String name;
        private final String description;
        private final Class<? extends AppCompatActivity> activityClass;

        ActivityInfo(String name, String description, Class<? extends AppCompatActivity> activityClass) {
            this.name = name;
            this.description = description;
            this.activityClass = activityClass;
        }
    }

    private static class ActivitiesAdapter extends RecyclerView.Adapter<ActivitiesAdapter.ViewHolder> {

        private final List<ActivityInfo> activities;

        ActivitiesAdapter(List<ActivityInfo> activities) {
            this.activities = activities;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ListItemActivityBinding binding = ListItemActivityBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
            );
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ActivityInfo activityInfo = activities.get(position);
            holder.binding.activityName.setText(activityInfo.name);
            holder.binding.activityDescription.setText(activityInfo.description);
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), activityInfo.activityClass);
                v.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return activities.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            private final ListItemActivityBinding binding;

            ViewHolder(ListItemActivityBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}

/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.libraries.places.samples.javaview;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.samples.javaview.core.DemoCategory;
import com.google.android.libraries.places.samples.javaview.core.DemoItem;
import com.google.android.libraries.places.samples.javaview.core.DemoRegistry;
import com.google.android.libraries.places.samples.javaview.databinding.ActivityMainBinding;
import com.google.android.libraries.places.samples.javaview.databinding.ItemDemoBinding;
import java.util.ArrayList;
import java.util.List;

/** Main entry point displaying a categorized and searchable menu of Places SDK Java View demos. */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private DemoAdapter adapter;
    private String currentQuery = "";
    private DemoCategory currentCategory = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (!Places.isInitialized()) {
            try {
                String apiKey = getPackageManager()
                    .getApplicationInfo(getPackageName(), android.content.pm.PackageManager.GET_META_DATA)
                    .metaData.getString("com.google.android.geo.API_KEY");
                if (apiKey != null && !apiKey.isEmpty()) {
                    Places.initialize(getApplicationContext(), apiKey);
                }
            } catch (Exception ignored) {
            }
        }

        setupRecyclerView();
        setupSearchAndFilter();
    }

    private void setupRecyclerView() {
        adapter = new DemoAdapter(DemoRegistry.getDemos(), demoItem -> {
            Intent intent = new Intent(MainActivity.this, demoItem.getActivityClass());
            startActivity(intent);
        });
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
    }

    private void setupSearchAndFilter() {
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s.toString();
                filterDemos();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.categoryChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty() || checkedIds.contains(R.id.chipAll)) {
                currentCategory = null;
            } else if (checkedIds.contains(R.id.chipCoreInit)) {
                currentCategory = DemoCategory.CORE_INITIALIZATION;
            } else if (checkedIds.contains(R.id.chipSearchDiscovery)) {
                currentCategory = DemoCategory.SEARCH_DISCOVERY;
            } else if (checkedIds.contains(R.id.chipDetailsPhotos)) {
                currentCategory = DemoCategory.PLACE_DETAILS_PHOTOS;
            } else if (checkedIds.contains(R.id.chipAttributesHours)) {
                currentCategory = DemoCategory.PLACE_ATTRIBUTES_HOURS;
            } else if (checkedIds.contains(R.id.chipUiKitActions)) {
                currentCategory = DemoCategory.PLACES_UI_KIT_ACTIONS;
            }
            filterDemos();
        });
    }

    private void filterDemos() {
        List<DemoItem> filtered = DemoRegistry.filterDemos(this, currentQuery, currentCategory);
        adapter.setDemos(filtered);
    }

    private static class DemoAdapter extends RecyclerView.Adapter<DemoAdapter.ViewHolder> {

        public interface OnItemClickListener {
            void onItemClick(DemoItem item);
        }

        private final List<DemoItem> items = new ArrayList<>();
        private final OnItemClickListener listener;

        public DemoAdapter(List<DemoItem> initialItems, OnItemClickListener listener) {
            this.items.addAll(initialItems);
            this.listener = listener;
        }

        public void setDemos(List<DemoItem> newItems) {
            this.items.clear();
            this.items.addAll(newItems);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemDemoBinding binding = ItemDemoBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DemoItem item = items.get(position);
            holder.binding.titleText.setText(item.getTitle());
            holder.binding.descriptionText.setText(item.getDescription());
            holder.binding.categoryChip.setText(item.getCategory().getTitleResId());
            holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final ItemDemoBinding binding;

            ViewHolder(ItemDemoBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}

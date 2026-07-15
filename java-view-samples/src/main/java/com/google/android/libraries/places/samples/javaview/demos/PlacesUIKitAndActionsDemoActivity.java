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

package com.google.android.libraries.places.samples.javaview.demos;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.samples.javaview.R;
import com.google.android.libraries.places.samples.javaview.core.Demo;
import com.google.android.libraries.places.samples.javaview.core.DemoCategory;
import com.google.android.libraries.places.samples.javaview.databinding.ActivityPlacesUikitAndActionsBinding;
import com.google.android.libraries.places.widget.AdvancedPlaceDetailsCompactFragment;
import com.google.android.libraries.places.widget.AdvancedPlaceDetailsFragment;
import com.google.android.libraries.places.widget.model.PlaceAction;
import com.google.android.libraries.places.widget.model.PlaceActionProvider;
import java.util.Arrays;
import java.util.List;

/** Demo Activity showcasing Places UI Kit fragments and custom PlaceActionProvider overrides. */
@Demo(
    category = DemoCategory.PLACES_UI_KIT_ACTIONS,
    title = "Places UI Kit & Action Providers",
    description = "Embed AdvancedPlaceDetailsFragment, CompactFragment, and custom PlaceActionProvider controls."
)
public class PlacesUIKitAndActionsDemoActivity extends AppCompatActivity {

    private ActivityPlacesUikitAndActionsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlacesUikitAndActionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

                
        binding.btnLoadFragment.setOnClickListener(v -> loadUIKitFragment());
        binding.customActionsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> loadUIKitFragment());

        loadUIKitFragment();
    }

    private void loadUIKitFragment() {
        String placeId = binding.placeIdEditText.getText() != null ?
            binding.placeIdEditText.getText().toString().trim() : "ChIJN1t_tDeuEmsRUsoyG83frY4";

        boolean isFull = binding.fragmentTypeToggle.getCheckedButtonId() == R.id.btnTypeFull;
        boolean enableCustomActions = binding.customActionsSwitch.isChecked();

        Fragment fragment;
        if (isFull) {
            AdvancedPlaceDetailsFragment fullFrag = AdvancedPlaceDetailsFragment.newInstance(
                AdvancedPlaceDetailsFragment.STANDARD_CONTENT);
            fullFrag.loadWithPlaceId(placeId);
            if (enableCustomActions) {
                fullFrag.setPlaceActionProvider(createCustomActionProvider());
            }
            fragment = fullFrag;
        } else {
            AdvancedPlaceDetailsCompactFragment compactFrag = AdvancedPlaceDetailsCompactFragment.newInstance(
                AdvancedPlaceDetailsCompactFragment.STANDARD_CONTENT);
            compactFrag.loadWithPlaceId(placeId);
            if (enableCustomActions) {
                compactFrag.setPlaceActionProvider(createCustomActionProvider());
            }
            fragment = compactFrag;
        }

        getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit();
    }

    private PlaceActionProvider createCustomActionProvider() {
        return new PlaceActionProvider() {
            @Override
            public void addPlaceActionsChangedListener(OnChangedListener listener) {}

            @Override
            public void removePlaceActionsChangedListener(OnChangedListener listener) {}

            @Override
            public List<PlaceAction> getMainPlaceActions(Place place) {
                PlaceAction customAction = PlaceAction.builder(
                    R.string.app_name,
                    (context, p) -> Toast.makeText(
                        context,
                        "Custom Action clicked for place: " + (p != null ? p.getDisplayName() : "Unknown"),
                        Toast.LENGTH_SHORT
                    ).show()
                ).build();

                return Arrays.asList(
                    PlaceAction.OPEN_DIRECTIONS,
                    PlaceAction.CALL,
                    customAction
                );
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_demo_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_reset_defaults) {
            getSharedPreferences("demo_prefs", MODE_PRIVATE).edit().clear().apply();
            resetInputsToDefault();
            Toast.makeText(this, "Reset to factory defaults", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.action_info) {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("ℹ️ Demo Instructions & Info")
                .setMessage("Main Point: Demonstrates AdvancedPlaceDetailsCompactFragment with custom PlaceActionProvider (CALL, WEBSITE, DIRECTIONS, MAPS).\n\nHow to Use: Interact with custom action buttons and adjust media/review ranking preferences.")
                .setPositiveButton("Got It", null)
                .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void resetInputsToDefault() {
        getSharedPreferences("demo_prefs", MODE_PRIVATE).edit().clear().apply();
    }
}

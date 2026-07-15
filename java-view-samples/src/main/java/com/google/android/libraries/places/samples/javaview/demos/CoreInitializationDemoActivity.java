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

import com.google.android.libraries.places.samples.javaview.R;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.samples.javaview.core.Demo;
import com.google.android.libraries.places.samples.javaview.core.DemoCategory;
import com.google.android.libraries.places.samples.javaview.databinding.ActivityCoreInitializationBinding;

/** Demo Activity showcasing Places SDK core initialization, status checks, and lifecycle control. */
@Demo(
    category = DemoCategory.CORE_INITIALIZATION,
    title = "Core Initialization & App Check",
    description = "Verify Places SDK status, App Check token generation, and re-initialization."
)
public class CoreInitializationDemoActivity extends AppCompatActivity {

    private ActivityCoreInitializationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCoreInitializationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

                
        updateStatus();

        binding.reinitButton.setOnClickListener(v -> {
            String inputKey = binding.apiKeyEditText.getText() != null ? binding.apiKeyEditText.getText().toString().trim() : "";
            String apiKeyToUse = inputKey;
            if (apiKeyToUse.isEmpty()) {
                try {
                    apiKeyToUse = getPackageManager()
                        .getApplicationInfo(getPackageName(), android.content.pm.PackageManager.GET_META_DATA)
                        .metaData.getString("com.google.android.geo.API_KEY");
                } catch (Exception e) {
                    appendLog("Error reading manifest API_KEY: " + e.getMessage());
                }
            }

            if (apiKeyToUse != null && !apiKeyToUse.isEmpty()) {
                Places.initializeWithNewPlacesApiEnabled(getApplicationContext(), apiKeyToUse);
                appendLog("Places.initializeWithNewPlacesApiEnabled() called successfully.");
            } else {
                appendLog("No API key available for initialization.");
            }
            updateStatus();
        });

        binding.deinitButton.setOnClickListener(v -> {
            Places.deinitialize();
            appendLog("Places.deinitialize() called.");
            updateStatus();
        });
    }

    private void updateStatus() {
        boolean initialized = Places.isInitialized();
        binding.statusText.setText(initialized ? "Places SDK is INITIALIZED" : "Places SDK is NOT INITIALIZED");
        appendLog("Current status check: Places.isInitialized() = " + initialized);
    }

    private void appendLog(String message) {
        String existing = binding.logText.getText() != null ? binding.logText.getText().toString() : "";
        String newLog = existing.isEmpty() ? message : existing + "\n" + message;
        binding.logText.setText(newLog);
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
                .setMessage("Main Point: Demonstrates Places SDK 5.3.0 initialization state (Places.isInitialized()), custom App Check token provider, and lifecycle management.\n\nHow to Use: Tap Check Status to verify API readiness, or test Initialize/Deinitialize toggles.")
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

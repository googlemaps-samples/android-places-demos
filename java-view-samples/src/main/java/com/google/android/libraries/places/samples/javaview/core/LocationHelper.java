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

package com.google.android.libraries.places.samples.javaview.core;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

/** Location helper providing FusedLocationProviderClient resolution and hardcoded fallback presets. */
public class LocationHelper {

    public static class PresetLocation {
        private final String name;
        private final LatLng latLng;

        public PresetLocation(String name, LatLng latLng) {
            this.name = name;
            this.latLng = latLng;
        }

        public String getName() {
            return name;
        }

        public LatLng getLatLng() {
            return latLng;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static final PresetLocation SYDNEY_OPERA_HOUSE =
        new PresetLocation("Sydney Opera House", new LatLng(-33.8568, 151.2153));
    public static final PresetLocation GOOGLEPLEX =
        new PresetLocation("Googleplex (Mountain View)", new LatLng(37.4220, -122.0841));
    public static final PresetLocation TOKYO_STATION =
        new PresetLocation("Tokyo Station", new LatLng(35.6812, 139.7671));
    public static final PresetLocation EMPIRE_STATE_BUILDING =
        new PresetLocation("Empire State Building", new LatLng(40.7484, -73.9857));

    public static final PresetLocation[] PRESETS = new PresetLocation[]{
        SYDNEY_OPERA_HOUSE,
        GOOGLEPLEX,
        TOKYO_STATION,
        EMPIRE_STATE_BUILDING
    };

    public static boolean hasLocationPermissions(Context context) {
        boolean fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED;
        return fine || coarse;
    }

    public static Task<LatLng> getCurrentLocationOrDefault(Context context) {
        if (!hasLocationPermissions(context)) {
            return Tasks.forResult(GOOGLEPLEX.getLatLng());
        }

        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(context);
        try {
            return client.getLastLocation().continueWith(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    return new LatLng(task.getResult().getLatitude(), task.getResult().getLongitude());
                }
                return GOOGLEPLEX.getLatLng();
            });
        } catch (SecurityException e) {
            return Tasks.forResult(GOOGLEPLEX.getLatLng());
        }
    }
}

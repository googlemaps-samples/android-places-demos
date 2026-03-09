// Copyright 2026 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.placesuikit3d

import com.google.android.gms.maps3d.model.LatLngAltitude

/**
 * A data class representing a landmark in the demo.
 *
 * @property id The unique Place ID for this landmark.
 * @property name The human-readable name of the landmark.
 * @property location The coordinates on the 3D map where the camera should point.
 */
data class Landmark(
    val id: String,
    val name: String,
    val location: LatLngAltitude
)

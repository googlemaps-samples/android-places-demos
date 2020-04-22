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

package com.example.placesdemo.model

/**
 * Location types for a reverse geocoding request. Please see [Reverse
 * Geocoding](https://developers.google.com/maps/documentation/geocoding/start#reverse) for more detail.
 */
enum class LocationType {
    /**
     * Restricts the results to addresses for which we have location information accurate down to
     * street address precision.
     */
    ROOFTOP,

    /**
     * Restricts the results to those that reflect an approximation (usually on a road) interpolated
     * between two precise points (such as intersections). An interpolated range generally indicates
     * that rooftop geocodes are unavailable for a street address.
     */
    RANGE_INTERPOLATED,

    /**
     * Restricts the results to geometric centers of a location such as a polyline (for example, a
     * street) or polygon (region).
     */
    GEOMETRIC_CENTER,

    /** Restricts the results to those that are characterized as approximate.  */
    APPROXIMATE,

    /**
     * Indicates an unknown location type returned by the server. The Java Client for Google Maps
     * Services should be updated to support the new value.
     */
    UNKNOWN;
}
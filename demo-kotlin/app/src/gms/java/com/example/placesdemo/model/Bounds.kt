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

import com.google.android.gms.maps.model.LatLng
import java.io.Serializable

/** The northeast and southwest points that delineate the outer bounds of a map.  */
class Bounds : Serializable {
    /** The northeast corner of the bounding box.  */
    var northeast: LatLng? = null

    /** The southwest corner of the bounding box.  */
    var southwest: LatLng? = null
    override fun toString(): String {
        return String.format("[%s, %s]", northeast, southwest)
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
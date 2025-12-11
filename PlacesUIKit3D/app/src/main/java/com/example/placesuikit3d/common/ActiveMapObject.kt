// Copyright 2025 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.placesuikit3d.common

import com.google.android.gms.maps3d.model.Marker
import com.google.android.gms.maps3d.model.Model
import com.google.android.gms.maps3d.model.Polygon
import com.google.android.gms.maps3d.model.Polyline

internal sealed class ActiveMapObject {
  abstract fun remove()

  data class ActiveMarker(val marker: Marker) : ActiveMapObject() {
    override fun remove() {
      marker.remove()
    }
  }

  data class ActivePolyline(val polyline: Polyline) : ActiveMapObject() {
    override fun remove() {
      polyline.remove()
    }
  }

  data class ActivePolygon(val polygon: Polygon) : ActiveMapObject() {
    override fun remove() {
      polygon.remove()
    }
  }

  data class ActiveModel(val model: Model) : ActiveMapObject() {
    override fun remove() {
      model.remove()
    }
  }
}
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

package com.example.placesdemo.programmatic_autocomplete

import com.google.android.gms.maps.model.LatLng
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException

/** Handle conversion from varying types of latitude and longitude representations.  */
class LatLngAdapter : TypeAdapter<LatLng?>() {
    /**
     * Reads in a JSON object and try to create a LatLng in one of the following formats.
     *
     * <pre>{
     * "lat" : -33.8353684,
     * "lng" : 140.8527069
     * }
     *
     * {
     * "latitude": -33.865257570508334,
     * "longitude": 151.19287000481452
     * }</pre>
     */
    @Throws(IOException::class)
    override fun read(reader: JsonReader): LatLng? {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }
        var lat = 0.0
        var lng = 0.0
        var hasLat = false
        var hasLng = false
        reader.beginObject()
        while (reader.hasNext()) {
            val name = reader.nextName()
            if ("lat" == name || "latitude" == name) {
                lat = reader.nextDouble()
                hasLat = true
            } else if ("lng" == name || "longitude" == name) {
                lng = reader.nextDouble()
                hasLng = true
            }
        }
        reader.endObject()
        return if (hasLat && hasLng) {
            LatLng(lat, lng)
        } else {
            null
        }
    }

    /** Not supported.  */
    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: LatLng?) {
        throw UnsupportedOperationException("Unimplemented method.")
    }
}
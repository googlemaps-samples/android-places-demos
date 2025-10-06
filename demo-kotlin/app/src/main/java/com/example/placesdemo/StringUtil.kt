/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.placesdemo

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.net.Uri
import android.text.TextUtils
import android.text.style.StyleSpan
import android.widget.TextView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.SearchNearbyResponse

/**
 * Utility class for converting objects to viewable strings and back.
 */
object StringUtil {
    private const val RESULT_SEPARATOR = "\n---\n\t"

    @SuppressLint("SetTextI18n")
    fun prepend(textView: TextView, prefix: String) {
        textView.text = """
            $prefix

            ${textView.text}
            """.trimIndent()
    }

    fun convertToLatLngBounds(
        southWest: String?, northEast: String?): LatLngBounds? {
        val southWestLatLng = convertToLatLng(southWest)
        val northEastLatLng = convertToLatLng(northEast)
        return if (southWestLatLng == null || northEastLatLng == null) {
            null
        } else LatLngBounds(southWestLatLng, northEastLatLng)
    }

    fun convertToLatLng(value: String?): LatLng? {
        if (TextUtils.isEmpty(value)) {
            return null
        }
        val split = value!!.split(",").dropLastWhile { it.isEmpty() }.toTypedArray()
        return if (split.size != 2) {
            null
        } else try {
            LatLng(split[0].toDouble(), split[1].toDouble())
        } catch (_: NullPointerException) {
            null
        } catch (_: NumberFormatException) {
            null
        }
    }

    fun countriesStringToArrayList(countriesString: String): List<String> {
        // Allow these delimiters: , ; | / \
        return listOf(*countriesString
            .replace("\\s".toRegex(), "|")
            .split("[,;|/\\\\]").dropLastWhile { it.isEmpty() }.toTypedArray())
    }

    fun stringify(response: SearchNearbyResponse, raw: Boolean): String {
        val builder = StringBuilder()
        val places = response.places
        builder
            .append(places.size)
            .append(" Nearby Places Results:")

        if (raw) {
            builder.append(RESULT_SEPARATOR)
            appendListToStringBuilder(builder, places)
        } else {
            for (place in places) {
                builder
                    .append(RESULT_SEPARATOR)
                    .append(place.displayName ?: "Unnamed Place")
                    .append(" (")
                    .append(place.id ?: "no_id")
                    .append(")")
            }
        }

        // Optionally include routing summaries if present
        val routingSummaries = response.routingSummaries
        if (!routingSummaries.isNullOrEmpty()) {
            builder.append(RESULT_SEPARATOR)
                .append(routingSummaries.size)
                .append(" Routing Summaries:")
            if (raw) {
                builder.append(RESULT_SEPARATOR)
                appendListToStringBuilder(builder, routingSummaries)
            } else {
                for (summary in routingSummaries) {
                    builder.append(RESULT_SEPARATOR)
                        .append(summary.toString())
                }
            }
        }

        return builder.toString()
    }


    fun stringify(response: FindAutocompletePredictionsResponse, raw: Boolean): String {
        val builder = StringBuilder()
        builder
            .append(response.autocompletePredictions.size)
            .append(" Autocomplete Predictions Results:")
        if (raw) {
            builder.append(RESULT_SEPARATOR)
            appendListToStringBuilder(builder, response.autocompletePredictions)
        } else {
            for (autocompletePrediction in response.autocompletePredictions) {
                builder
                    .append(RESULT_SEPARATOR)
                    .append(autocompletePrediction.getFullText( /* matchStyle */null))
            }
        }
        return builder.toString()
    }

    fun stringify(response: FetchPlaceResponse, raw: Boolean): String {
        val builder = StringBuilder()
        builder.append("Fetch Place Result:").append(RESULT_SEPARATOR)
        if (raw) {
            builder.append(response.place)
        } else {
            builder.append(stringify(response.place))
        }
        return builder.toString()
    }

    fun stringify(place: Place): String {
        return "${place.displayName?.plus(" (") ?: ""}${place.formattedAddress?.plus(")") ?: ""}"
    }

    fun stringify(place: AutocompletePrediction?): String {
        val primary = place?.getPrimaryText(StyleSpan(Typeface.BOLD)).toString()
        val secondary = place?.getSecondaryText(StyleSpan(Typeface.NORMAL)).toString()
        return "$primary ($secondary)"
    }

    fun stringify(uri: Uri): String {
        return uri.toString()
    }

    fun stringifyAutocompleteWidget(place: Place, raw: Boolean): String {
        val builder = StringBuilder()
        builder.append("Autocomplete Widget Result:").append(RESULT_SEPARATOR)
        if (raw) {
            builder.append(place)
        } else {
            builder.append(stringify(place))
        }
        return builder.toString()
    }

    fun stringifyAutocompletePrediction(place: AutocompletePrediction?, raw: Boolean): String {
        val builder = StringBuilder()
        builder.append("Autocomplete Prediction Result:").append(RESULT_SEPARATOR)
        if (raw) {
            builder.append(place)
        } else {
            builder.append(stringify(place))
        }
        return builder.toString()
    }

    private fun <T> appendListToStringBuilder(builder: StringBuilder, items: List<T>) {
        if (items.isEmpty()) {
            return
        }
        builder.append(items[0])
        for (i in 1 until items.size) {
            builder.append(RESULT_SEPARATOR)
            builder.append(items[i])
        }
    }
}
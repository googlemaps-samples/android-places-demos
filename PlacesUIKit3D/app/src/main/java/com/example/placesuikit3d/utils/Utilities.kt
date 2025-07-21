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

package com.example.placesuikit3d.utils

import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.FlyAroundOptions
import com.google.android.gms.maps3d.model.FlyToOptions
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyAroundOptions
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import java.util.Locale
import kotlin.math.floor

val headingRange = 0.0..360.0
val tiltRange = 0.0..90.0
val rangeRange = 0.0..63170000.0
val rollRange = -360.0..360.0

val latitudeRange = -90.0..90.0
val longitudeRange = -180.0..180.0
val altitudeRange = 0.0..LatLngAltitude.MAX_ALTITUDE_METERS

const val DEFAULT_HEADING = 0.0
const val DEFAULT_TILT = 60.0
const val DEFAULT_RANGE = 1500.0
const val DEFAULT_ROLL = 0.0

/**
 * Converts a nullable Camera object into a valid, non-null Camera object.
 * If the input is null, returns the DEFAULT_CAMERA configuration.
 * If the input is non-null, validates its components (center, heading, tilt, roll, range)
 * using helper functions (toValidLocation, toHeading, toTilt, toRoll, toRange).
 *
 * @receiver The nullable Camera object to validate.
 * @return A valid, non-null Camera object.
 */
fun Camera?.toValidCamera(): Camera {
    // Use elvis operator for concise null handling
    val source = this ?: return Camera.DEFAULT_CAMERA // Return default camera if source is null

    // If source is not null, validate its components
    return camera {
        // Validate center using the provided toValidLocation function
        center = source.center.toValidLocation()
        // Validate orientation and range using the existing to...() functions
        heading = source.heading.toHeading()
        tilt = source.tilt.toTilt()
        roll = source.roll.toRoll()
        range = source.range.toRange()
    }
}

/**
 * Coerces the latitude, longitude, and altitude of a LatLngAltitude object
 * to be within their valid ranges. Longitude is clamped, not wrapped here.
 *
 * @receiver The LatLngAltitude to validate.
 * @return A new LatLngAltitude object with validated components.
 */
fun LatLngAltitude.toValidLocation(): LatLngAltitude {
    val objectToCopy = this
    return latLngAltitude {
        // Coerce latitude within -90.0 to 90.0
        latitude = objectToCopy.latitude.coerceIn(latitudeRange)
        // Coerce longitude within -180.0 to 180.0 (Note: wrapping might be preferred sometimes)
        longitude = objectToCopy.longitude.coerceIn(longitudeRange)
        // Coerce altitude within 0.0 to MAX_ALTITUDE_METERS
        altitude = objectToCopy.altitude.coerceIn(altitudeRange)
    }
}

/**
 * Converts a Number? to a valid heading value (0.0 to 360.0).
 * Returns 0.0 if the input is null.
 * Uses wrapIn to ensure the value is within the headingRange.
 *
 * @receiver The Number? to convert.
 * @return The heading value as a Double within [0.0, 360.0).
 */
fun Number?.toHeading(): Double =
    this?.toDouble()?.wrapIn(headingRange.start, headingRange.endInclusive) ?: DEFAULT_HEADING

/**
 * Converts a Number? to a valid tilt value (0.0 to 90.0).
 * Returns 0.0 if the input is null.
 * Clamps the value to the tiltRange, as tilt doesn't typically wrap.
 *
 * @receiver The Number? to convert.
 * @return The tilt value as a Double clamped within [0.0, 90.0].
 */
fun Number?.toTilt(): Double = this?.toDouble()?.coerceIn(tiltRange) ?: DEFAULT_TILT

/**
 * Converts a Number? to a valid roll value (-360.0 to 360.0 or often -180..180).
 * Returns 0.0 if the input is null.
 * Uses wrapIn to ensure the value is within the rollRange.
 * Consider using -180..180 range and wrapIn(lower, upper) for standard roll representation.
 *
 * @receiver The Number? to convert.
 * @return The roll value as a Double within the defined rollRange.
 */
fun Number?.toRoll(): Double = this?.toDouble()?.wrapIn(rollRange) ?: DEFAULT_ROLL

/**
 * Converts a Number? to a valid range value (0.0 to ~63,170,000.0).
 * Returns 0.0 if the input is null.
 * Clamps the value to the rangeRange, as range/distance doesn't wrap.
 *
 * @receiver The Number? to convert.
 * @return The range value as a Double clamped within the defined rangeRange.
 */
fun Number?.toRange(): Double = this?.toDouble()?.coerceIn(rangeRange) ?: DEFAULT_RANGE

// Assumes we are close to the range
fun Double.wrapIn(range: ClosedFloatingPointRange<Double>): Double {
    var answer = this
    val delta = range.endInclusive - range.start
    while (answer > range.endInclusive) {
        answer -= delta
    }
    while (answer < range.start) {
        answer += delta
    }

    return answer
}

/**
 * Wraps a Float value within a specified range.
 * If the value is outside the range, it is adjusted by repeatedly adding or subtracting
 * the range's span (delta) until it falls within the range.
 *
 * @param range The ClosedFloatingPointRange within which to wrap the value.
 * @return The wrapped Float value, guaranteed to be within the specified range.
 */
fun Float.wrapIn(range: ClosedFloatingPointRange<Float>): Float {
    var answer = this
    val delta = range.endInclusive - range.start
    while (answer > range.endInclusive) {
        answer -= delta
    }
    while (answer < range.start) {
        answer += delta
    }

    return answer
}

/**
 * Wraps a Double value within the specified range [lower, upper).
 * This method ensures that the returned value always falls within the specified range.
 * If the value is outside the range, it will be "wrapped around" to fit within the range.
 * For example, if the range is [0.0, 360.0) and the input is 370.0, the output will be 10.0.
 * If the range is [0.0, 360.0) and the input is -10.0, the output will be 350.0.
 *
 * @param lower The lower bound of the range (inclusive).
 * @param upper The upper bound of the range (exclusive).
 * @return The wrapped value within the range [lower, upper).
 * @throws IllegalArgumentException if the upper bound is not greater than the lower bound.
 */
fun Double.wrapIn(lower: Double, upper: Double): Double {
    val range = upper - lower
    if (range <= 0) {
        throw IllegalArgumentException("Upper bound must be greater than lower bound")
    }
    val offset = this - lower
    return lower + (offset - floor(offset / range) * range)
}

/**
 * Extension function on Number to get the nearest compass direction string
 * from a given heading in degrees.
 *
 * 0 degrees is North, 90 is East, 180 is South, 270 is West.
 * Handles headings outside the standard 0-360 range (e.g., -90 or 450 degrees).
 *
 * @return A string representing the nearest compass direction (e.g., "N", "NNE", "NE").
 */
fun Number.toCompassDirection(): String {
    val directions = listOf(
        "N", "NNE", "NE", "ENE",
        "E", "ESE", "SE", "SSE",
        "S", "SSW", "SW", "WSW",
        "W", "WNW", "NW", "NNW"
    )

    val headingDegrees = this.toDouble()

    // Normalize heading to 0-359.99... degrees
    val normalizedHeading = (headingDegrees % 360.0 + 360.0) % 360.0

    // Each of the 16 directions covers an arc of 360/16 = 22.5 degrees.
    // We add half of this (11.25) to the normalized heading before dividing
    // to correctly align with the center of each compass arc.
    val segment = 22.5
    val index = floor((normalizedHeading + (segment / 2)) / segment).toInt() % directions.size

    return directions[index]
}

/**
 * Creates a new [Camera] object by copying the current [Camera] and optionally overriding
 * its center, heading, tilt, range, and roll properties.
 *
 * @param center The new center [LatLngAltitude] to use, or null to keep the current center.
 * @param heading The new heading (bearing) to use, or null to keep the current heading.
 * @param tilt The new tilt (pitch) to use, or null to keep the current tilt.
 * @param range The new range (distance from the center) to use, or null to keep the current range.
 * @param roll The new roll to use, or null to keep the current roll.
 * @return A new [Camera] object with the specified properties updated.
 */
fun Camera.copy(
    center: LatLngAltitude? = null,
    heading: Double? = null,
    tilt: Double? = null,
    range: Double? = null,
    roll: Double? = null,
): Camera {
    val objectToCopy = this
    return camera {
        this.center = center ?: objectToCopy.center
        this.heading = heading ?: objectToCopy.heading
        this.tilt = tilt ?: objectToCopy.tilt
        this.range = range ?: objectToCopy.range
        this.roll = roll ?: objectToCopy.roll
    }
}

fun FlyAroundOptions.copy(
    center: Camera? = null,
    durationInMillis: Long? = null,
    rounds: Double? = null,
) : FlyAroundOptions {
    val objectToCopy = this

    return flyAroundOptions {
        this.center = (center ?: objectToCopy.center)
        this.durationInMillis = durationInMillis ?: objectToCopy.durationInMillis
        this.rounds = rounds ?: objectToCopy.rounds
    }
}

fun FlyToOptions.copy(
    endCamera: Camera? = null,
    durationInMillis: Long? = null,
) : FlyToOptions {
    val objectToCopy = this

    return flyToOptions {
        this.endCamera = (endCamera ?: objectToCopy.endCamera)
        this.durationInMillis = durationInMillis ?: objectToCopy.durationInMillis
    }
}

/**
 * Converts a [Camera] object to a formatted string representation.
 *
 * This function takes a [Camera] object, validates it using [toValidCamera], and then
 * constructs a multi-line string that represents the camera's properties in a human-readable
 * format. The string includes the camera's center (latitude, longitude, altitude),
 * heading, tilt, and range.
 *
 * The latitude, longitude, altitude, heading, tilt, and range are formatted to specific
 * decimal places for readability (6, 6, 1, 0, 0, 0 respectively).
 *
 * The output string is designed to be easily copied and pasted directly into code to recreate
 * a [Camera] object with the same parameters. This is especially useful for quickly positioning
 * the camera to a specific view.
 *
 * Example output:
 * ```
 * camera {
 *     center = latLngAltitude {
 *         latitude = 34.052235
 *         longitude = -118.243685
 *         altitude = 100.0
 *     }
 *     heading = 90
 *     tilt = 45
 *     range = 5000
 * }
 * ```
 *
 * @receiver The [Camera] object to convert.
 * @return A string representation of the [Camera] object, suitable for pasting into source code.
 */
fun Camera.toCameraString(): String {
    val camera = this.toValidCamera()
    return """
        camera {
            center = latLngAltitude {
                latitude = ${camera.center.latitude.format(6)}
                longitude = ${camera.center.longitude.format(6)}
                altitude = ${camera.center.altitude.format(1)}
            }
            heading = ${camera.heading.format(0)}
            tilt = ${camera.tilt.format(0)}
            range = ${camera.range.format(0)}
        }""".trimIndent()
}

/**
 * Formats a nullable Double to a string with a specified number of decimal places.
 *
 * If the Double is null, returns "null".
 * If decimalPlaces is 0, it formats the number with no decimal places and appends ".0".
 * If decimalPlaces is greater than 0, it formats the number with the specified number of decimal places.
 *
 * Note, this is intended for logging and debugging not for display to the user.
 *
 * @receiver The nullable Double to format.
 * @param decimalPlaces The number of decimal places to include in the formatted string.
 * @return The formatted string representation of the Double, or "null" if the input is null.
 */
internal fun Double?.format(decimalPlaces: Int): String {
    if (this == null) return "null"

    return if (decimalPlaces == 0) {
        String.format(Locale.US, "%.0f.0", this)
    } else {
        String.format(Locale.US, "%.${decimalPlaces}f", this)
    }
}

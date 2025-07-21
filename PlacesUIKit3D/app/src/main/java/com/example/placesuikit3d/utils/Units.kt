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


import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.res.stringResource
import com.example.placesuikit3d.R

const val METERS_PER_FOOT = 3.28084
const val METERS_PER_KILOMETER = 1_000
const val FEET_PER_METER = 1 / METERS_PER_FOOT
const val FEET_PER_MILE = 5_280
const val MILES_PER_METER = 0.000621371

/** A value class to wrap a value representing a measurement in meters. */
@Immutable
@JvmInline
value class Meters(val value: Double) : Comparable<Meters> {
    override fun compareTo(other: Meters) = value.compareTo(other.value)

    operator fun minus(other: Meters) = Meters(value = this.value - other.value)
}

/** Create a Meters class from a [Number] */
@Stable
inline val Number.meters: Meters
    get() = Meters(value = this.toDouble())

/** Create a Meters class from a [Number] */
@Stable
inline val Number.m: Meters
    get() = Meters(value = this.toDouble())

/** Create a Meters class from a [Number] of kilometers */
@Stable
inline val Number.km: Meters
    get() = Meters(value = this.toDouble() * METERS_PER_KILOMETER)

/** Create a Meters class from a [Number] of feet */
@Stable
inline val Number.feet: Meters
    get() = Meters(value = this.toDouble() * FEET_PER_METER)

/** Create a Meters class from a [Number] of miles */
@Stable
inline val Number.miles: Meters
    get() = Meters(value = this.toDouble() / MILES_PER_METER)

/** Gets the number of equivalent feet from a meters value class */
@Stable
inline val Meters.toFeet: Double
    get() = value * METERS_PER_FOOT

/** Gets the value of a meters class as a Double */
@Stable
inline val Meters.toMeters: Double
    get() = value

/** Gets the number of equivalent kilometers from a meters value class */
@Stable
inline val Meters.toKilometers: Double
    get() = value / METERS_PER_KILOMETER

/** Gets the number of equivalent kilometers from a meters value class */
@Stable
inline val Meters.toMiles: Double
    get() = (value * MILES_PER_METER)

@Stable
operator fun Meters.plus(other: Meters) = Meters(value = this.value + other.value)

/**
 * A data class representing a value with a string resource ID for its units template.
 *
 * @property value: The numerical value.
 * @property unitsTemplate: The string resource ID for the units.
 */
data class ValueWithUnitsTemplate(val value: Double, @StringRes val unitsTemplate: Int)

/** Abstract base class for all units converters. */
abstract class UnitsConverter {
    abstract fun toDistanceUnits(meters: Meters): ValueWithUnitsTemplate

    abstract fun toElevationUnits(meters: Meters): ValueWithUnitsTemplate

    @Composable
    fun toDistanceString(meters: Meters): String {
        val (value, resourceId) = toDistanceUnits(meters = meters)
        return stringResource(id = resourceId, value)
    }

    fun toDistanceString(resources: Resources, meters: Meters): String {
        val (value, resourceId) = toDistanceUnits(meters = meters)
        return resources.getString(resourceId, value)
    }

    @Composable
    fun toElevationString(meters: Meters): String {
        val (value, resourceId) = toElevationUnits(meters = meters)
        return stringResource(id = resourceId, value)
    }
}

/**
 * Returns the appropriate [UnitsConverter] based on the given country code.
 *
 * @param countryCode The country code to determine the units converter for.
 * @return The appropriate [UnitsConverter] for the specified country code.
 */
fun getUnitsConverter(countryCode: String?): UnitsConverter {
    // TODO: other counties that use imperial units for distances?
    return if (countryCode == "US") {
        ImperialUnitsConverter
    } else {
        MetricUnitsConverter
    }
}

/** Class to render measurements in imperial units. */
object ImperialUnitsConverter : UnitsConverter() {
    override fun toDistanceUnits(meters: Meters): ValueWithUnitsTemplate {
        return if (meters < 0.25.miles) {
            ValueWithUnitsTemplate(meters.toFeet, R.string.in_feet)
        } else {
            ValueWithUnitsTemplate(meters.toMiles, R.string.in_miles)
        }
    }

    override fun toElevationUnits(meters: Meters): ValueWithUnitsTemplate {
        return ValueWithUnitsTemplate(meters.toFeet, R.string.in_feet)
    }
}

/** Class to render measurements in metric units. */
object MetricUnitsConverter : UnitsConverter() {
    override fun toDistanceUnits(meters: Meters): ValueWithUnitsTemplate {
        return if (meters < 1000.meters) {
            ValueWithUnitsTemplate(meters.toMeters, R.string.in_meters)
        } else {
            ValueWithUnitsTemplate(meters.toKilometers, R.string.in_kilometers)
        }
    }

    override fun toElevationUnits(meters: Meters): ValueWithUnitsTemplate {
        return ValueWithUnitsTemplate(meters.toMeters, R.string.in_meters)
    }
}

/** A composition local that provides a [UnitsConverter] instance. */
val LocalUnitsConverter = compositionLocalOf<UnitsConverter> { MetricUnitsConverter }

/** Creates a string to show the distance formatted with units */
@Composable
fun Meters.toDistanceString(): String {
    return LocalUnitsConverter.current.toDistanceString(this)
}

@Composable
fun Meters.toElevationString(): String {
    return LocalUnitsConverter.current.toElevationString(this)
}

operator fun Meters.plus(value: Number) = Meters(this.value + value.toDouble())

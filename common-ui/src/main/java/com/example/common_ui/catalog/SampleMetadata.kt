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

package com.example.common_ui.catalog

import java.io.Serializable

/**
 * Annotation for Google Maps Platform Places sample activities and snippet entry points.
 *
 * Provides structured metadata consumed by the Unified Sample Catalog and the on-device Reviewer Mode.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Sample(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val complexity: Complexity = Complexity.SIMPLE,
    val tags: Array<String> = [],
    val apiCalls: Array<String> = [],
    val purpose: String = "",
    val successCriteria: String = "",
    val failureIndicators: String = "",
    val framework: Framework = Framework.KOTLIN_VIEWS,
)

/**
 * Converts a [Sample] annotation on an Activity class into a [SampleItem] instance.
 */
fun Sample.toSampleItem(activityClassName: String): SampleItem = SampleItem(
    id = id,
    title = title,
    description = description,
    category = category,
    complexity = complexity,
    tags = tags.toList(),
    apiCalls = apiCalls.toList(),
    purpose = purpose,
    successCriteria = successCriteria,
    failureIndicators = failureIndicators,
    kotlinActivity = if (framework == Framework.KOTLIN_VIEWS) activityClassName else null,
    javaActivity = if (framework == Framework.JAVA_VIEWS) activityClassName else null,
    composeActivity = if (framework == Framework.KOTLIN_COMPOSE) activityClassName else null,
)

/**
 * Complexity level of a sample, allowing developers to filter between focused API snippets,
 * simple single-feature demos, and advanced end-to-end sample applications.
 */
enum class Complexity(val displayName: String, val badge: String) : Serializable {
    SNIPPET("Snippet", "🔹"),
    SIMPLE("Simple", "🟢"),
    ADVANCED("Advanced", "🔴"),
}

/**
 * UI Framework and language implementation target.
 *
 * Supports the three implementation paradigms across the Android Places Demos repository:
 * - [KOTLIN_VIEWS]: Kotlin using traditional Android Views / XML layouts / DialogFragments
 * - [JAVA_VIEWS]: Java using traditional Android Views / XML layouts
 * - [KOTLIN_COMPOSE]: Kotlin using Jetpack Compose
 */
enum class Framework(val displayName: String, val badge: String) : Serializable {
    KOTLIN_VIEWS("Kotlin Views", "💜 Kotlin Views"),
    JAVA_VIEWS("Java Views", "☕ Java Views"),
    KOTLIN_COMPOSE("Jetpack Compose", "🧩 Compose"),
}

/**
 * Reviewer evaluation status for a sample.
 */
enum class ReviewStatus(val displayName: String) : Serializable {
    UNCHECKED("Unchecked"),
    PASSING("Passing"),
    NEEDS_WORK("Needs Work"),
    ;

    companion object {
        fun fromString(value: String?): ReviewStatus = entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: UNCHECKED
    }
}

/**
 * Rich metadata describing a sample in the Unified Sample Catalog.
 */
data class SampleItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val complexity: Complexity,
    val tags: List<String>,
    val apiCalls: List<String> = emptyList(),
    val purpose: String,
    val successCriteria: String,
    val failureIndicators: String = "",
    val kotlinActivity: String? = null,
    val javaActivity: String? = null,
    val composeActivity: String? = null,
) : Serializable {
    /**
     * Returns the target Activity class name for the requested [framework], or null if not implemented.
     */
    fun getActivityForFramework(framework: Framework): String? = when (framework) {
        Framework.KOTLIN_VIEWS -> kotlinActivity
        Framework.JAVA_VIEWS -> javaActivity
        Framework.KOTLIN_COMPOSE -> composeActivity
    }

    /**
     * Returns the canonical target identifier (`<applicationId>/<activityFqcn>`) for the given [framework]
     * to use as a unique key for isolated framework evaluations.
     */
    fun getTargetFqcn(framework: Framework): String {
        val activity = getActivityForFramework(framework) ?: return "${id}_${framework.name}"
        val pkg = resolveTargetPackage(activity, framework, "")
        return if (pkg.isNotEmpty()) "$pkg/$activity" else "${activity}_${framework.name}"
    }

    /**
     * Returns true if this sample has implementations in more than one framework.
     */
    fun hasMultipleFrameworks(): Boolean {
        val count = listOfNotNull(kotlinActivity, javaActivity, composeActivity).size
        return count > 1
    }

    /**
     * Returns all frameworks implemented by this sample other than [currentFramework].
     */
    fun getAlternativeFrameworks(currentFramework: Framework): List<Framework> = Framework.entries.filter { it != currentFramework && getActivityForFramework(it) != null }

    /**
     * Formats the sample's purpose, verification criteria, and failure indicators as HTML.
     */
    fun getFormattedHelpHtml(isReviewerMode: Boolean = true): String = buildString {
        append("<b>Category:</b> ").append(category)
        append(" &bull; <b>Complexity:</b> ").append(complexity.badge).append(" ").append(complexity.displayName)
        append("<br/><br/>")
        append("<b>Purpose:</b><br/>").append(purpose)
        if (isReviewerMode) {
            if (successCriteria.isNotBlank()) {
                append("<br/><br/><b>✅ What to Verify (Success Criteria):</b><br/>")
                append(successCriteria.replace("\n", "<br/>"))
            }
            if (failureIndicators.isNotBlank()) {
                append("<br/><br/><b>⚠️ Failure Indicators:</b><br/>")
                append(failureIndicators.replace("\n", "<br/>"))
            }
        }
        if (tags.isNotEmpty()) {
            append("<br/><br/><b>Tags:</b> ").append(tags.joinToString(" "))
        }
    }

    companion object {
        /**
         * Resolves the Android application package ID that hosts [activityClassName] for [framework].
         */
        @JvmStatic
        fun resolveTargetPackage(activityClassName: String, framework: Framework, fallbackPackage: String): String = when {
            activityClassName.startsWith("com.example.placesdemo.") -> {
                if (framework == Framework.JAVA_VIEWS) {
                    "com.example.placesdemo.java"
                } else {
                    "com.example.placesdemo"
                }
            }

            activityClassName.startsWith("com.example.placedetailsuikit.") -> "com.example.placedetailsuikit"

            activityClassName.startsWith(
                "com.google.places.android.ktx.demo.",
            ) -> "com.google.maps.android.ktx.demo"

            activityClassName.startsWith("com.example.placedetailscompose.") -> "com.example.placedetailscompose"

            activityClassName.startsWith("com.example.placesuikit3d.") -> "com.example.placesuikit3d"

            activityClassName.startsWith("com.google.places.") -> "com.google.places"

            else -> fallbackPackage
        }
    }
}

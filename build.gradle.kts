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

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.secrets.gradle.plugin) apply false
    alias(libs.plugins.jetbrains.kotlin.parcelize) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.spotless)
}

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**")
        ktlint().editorConfigOverride(
            mapOf(
                "indent_size" to "4",
                "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
                "max_line_length" to "120",
                "ktlint_standard_max-line-length" to "disabled",
                // Samples use the `programmatic_autocomplete` package; renaming it would
                // change public sample paths.
                "ktlint_standard_package-name" to "disabled",
                // Samples use instance-level `TAG` properties for logging.
                "ktlint_standard_property-naming" to "disabled",
                // Samples use `_state` private flows without a matching public property.
                "ktlint_standard_backing-property-naming" to "disabled",
            ),
        )
        trimTrailingWhitespace()
        endWithNewline()
    }
    java {
        target("**/*.java")
        targetExclude("**/build/**")
        googleJavaFormat().aosp()
    }
}

allprojects {
    configurations.all {
        resolutionStrategy {
            force("org.jetbrains.kotlin:kotlin-metadata-jvm:2.4.21")
        }
    }
}

subprojects {
    // Skip the AAR metadata AGP version check while still allowing Gradle to create
    // the task's @OutputDirectory (required by library modules' bundle<Variant>Aar task).
    plugins.withId("com.android.application") {
        tasks.matching { it.name.startsWith("check") && it.name.endsWith("AarMetadata") }
            .configureEach {
                enabled = false
            }
    }
    plugins.withId("com.android.library") {
        tasks.matching { it.name.startsWith("check") && it.name.endsWith("AarMetadata") }
            .configureEach {
                actions.clear()
                doLast {}
            }
    }
}

val appModules = listOf(
    ":demo-kotlin",
    ":demo-java",
    ":PlaceDetailsUIKit",
    ":kotlin-demos",
    ":PlaceDetailsCompose",
    ":PlacesUIKit3D",
    ":snippets"
)

tasks.register("installAllDebug") {
    group = "application"
    description = "Installs debug APKs for all 7 Places demo modules so cross-app launching in the Unified Catalog works seamlessly."
    dependsOn(appModules.map { "$it:installDebug" })
}

tasks.register("installAndLaunch") {
    group = "application"
    description = "Installs all Places demo APKs and launches the Unified Sample Catalog (or a specific sample via -Psample=... -Pframework=...)."
    dependsOn("installAllDebug")
    dependsOn(":demo-kotlin:installAndLaunch")
}

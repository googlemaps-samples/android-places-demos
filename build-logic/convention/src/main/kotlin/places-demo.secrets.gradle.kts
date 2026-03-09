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

import java.util.Properties
import org.gradle.api.GradleException
import org.gradle.api.provider.ListProperty

interface SecretsVerificationExtension {
    val requiredKeys: ListProperty<String>
    val optionalKeys: ListProperty<String>
}

val secretsVerification = extensions.create<SecretsVerificationExtension>("secretsVerification")
secretsVerification.requiredKeys.convention(listOf("PLACES_API_KEY", "MAPS_API_KEY"))

// Optional keys:
// MAPS3D_API_KEY: Needed for the 'PlacesUIKit3D' demo.
// MAP_ID: Needed for the 'PlaceDetailsCompose' demo.
secretsVerification.optionalKeys.convention(listOf("MAPS3D_API_KEY", "MAP_ID"))

// Check for secrets.properties file and valid API key before proceeding with build tasks.
afterEvaluate {
    val requiredKeysToCheck = secretsVerification.requiredKeys.get()
    val optionalKeysToCheck = secretsVerification.optionalKeys.get()

    val secretsFile = rootProject.file("secrets.properties")
    val isCI = System.getenv("CI")?.toBoolean() ?: false

    if (!isCI) {
        val requestedTasks = gradle.startParameter.taskNames
        if (requestedTasks.isEmpty() && !secretsFile.exists()) {
            // It's likely an IDE sync if no tasks are specified, so just issue a warning.
            println("Warning: secrets.properties not found. Gradle sync may succeed, but building/running the app will fail.")
        } else if (requestedTasks.isNotEmpty()) {
            val buildTaskKeywords = listOf("build", "install", "assemble")
            val isBuildTask = requestedTasks.any { task ->
                buildTaskKeywords.any { keyword ->
                    task.contains(keyword, ignoreCase = true)
                }
            }

            val testTaskKeywords = listOf("test", "report", "lint")
            val isTestTask = requestedTasks.any { task ->
                testTaskKeywords.any { keyword ->
                    task.contains(keyword, ignoreCase = true)
                }
            }

            val isDebugTask = requestedTasks.any { task ->
                task.contains("Debug", ignoreCase = true) || task.contains("installAndLaunch", ignoreCase = true)
            }

            if (isBuildTask && !isTestTask && isDebugTask) {
                if (!secretsFile.exists()) {
                    val defaultsFile = rootProject.file("local.defaults.properties")
                    val requiredKeysMessage = if (defaultsFile.exists()) {
                        defaultsFile.readText()
                    } else {
                        requiredKeysToCheck.joinToString("\n") { "$it=<YOUR_API_KEY>" }
                    }
                    throw GradleException("secrets.properties file not found. Please create a 'secrets.properties' file in the root project directory with the following content:\n\n$requiredKeysMessage")
                }

                val secrets = Properties()
                secretsFile.inputStream().use { secrets.load(it) }

                val isValidKey = { key: String? ->
                    !key.isNullOrBlank() && key != "DEFAULT_API_KEY" && key!!.matches(Regex("^AIza[a-zA-Z0-9_-]{35}$"))
                }
                
                val isPresentAndNotDefault = { key: String? ->
                    !key.isNullOrBlank() && key != "DEFAULT_API_KEY"
                }

                requiredKeysToCheck.forEach { reqKey ->
                    val keyValue = secrets.getProperty(reqKey)
                    if (reqKey.endsWith("API_KEY", ignoreCase = true)) {
                        if (!isValidKey(keyValue)) {
                            throw GradleException("Invalid or missing $reqKey in secrets.properties. Please provide a valid Google Maps API key (starts with 'AIza').")
                        }
                    } else {
                        if (!isPresentAndNotDefault(keyValue)) {
                            throw GradleException("Missing $reqKey in secrets.properties.")
                        }
                    }
                }

                optionalKeysToCheck.forEach { optKey ->
                    val keyValue = secrets.getProperty(optKey)
                    if (isPresentAndNotDefault(keyValue)) {
                        if (optKey.endsWith("API_KEY", ignoreCase = true)) {
                            if (!isValidKey(keyValue)) {
                                val demoMsg = if (optKey == "MAPS3D_API_KEY") " (Required for PlacesUIKit3D demo)" else ""
                                throw GradleException("Invalid $optKey in secrets.properties.$demoMsg Please provide a valid Google Maps API key (starts with 'AIza').")
                            }
                        }
                    } else {
                        if (optKey == "MAPS3D_API_KEY") {
                            println("Warning: MAPS3D_API_KEY is missing or set to default in secrets.properties. The 'PlacesUIKit3D' demo will fail to load 3D maps at runtime.")
                        } else if (optKey == "MAP_ID") {
                            println("Warning: MAP_ID is missing or set to default in secrets.properties. The 'PlaceDetailsCompose' demo will fail to load custom map styling at runtime.")
                        }
                    }
                }
            }
        }
    }
}

plugins {
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

secrets {
    // To add your Google Maps Platform API key to this project:
    // 1. Copy local.defaults.properties to secrets.properties
    // 2. In the secrets.properties file, replace PLACES_API_KEY=DEFAULT_API_KEY with a key from a
    //    project with Places API enabled
    // 3. In the secrets.properties file, replace MAPS_API_KEY=DEFAULT_API_KEY with a key from a
    //    project with Maps SDK for Android enabled (can be the same project and key as in Step 2)
    defaultPropertiesFileName = "local.defaults.properties"

    // Optionally specify a different file name containing your secrets.
    // The plugin defaults to "local.properties"
    propertiesFileName = "secrets.properties"
}

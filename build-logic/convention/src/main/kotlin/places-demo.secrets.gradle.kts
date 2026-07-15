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

import java.io.File
import java.util.Properties
import org.gradle.api.GradleException
import org.gradle.api.provider.ListProperty

interface SecretsVerificationExtension {
    val requiredKeys: ListProperty<String>
    val optionalKeys: ListProperty<String>
}

val secretsVerification = extensions.create<SecretsVerificationExtension>("secretsVerification")
secretsVerification.requiredKeys.convention(listOf("PLACES_API_KEY", "MAPS_API_KEY"))
secretsVerification.optionalKeys.convention(listOf("MAPS3D_API_KEY", "MAP_ID"))

// Auto-sync keys from /usr/local/google/home/dkhawk/secrets.txt and enforce key validation on build/install tasks.
afterEvaluate {
    val requiredKeysToCheck = secretsVerification.requiredKeys.get()
    val optionalKeysToCheck = secretsVerification.optionalKeys.get()

    val secretsFile = rootProject.file("secrets.properties")
    val dkhawkSecretsFile = File("/usr/local/google/home/dkhawk/secrets.txt")

    // Helper to validate API key format (starts with AIza and has 39 chars)
    val isValidApiKey = { key: String? ->
        !key.isNullOrBlank() && key != "YOUR_API_KEY" && key != "DEFAULT_API_KEY" && key.matches(Regex("^AIza[a-zA-Z0-9_-]{35}$"))
    }

    val isPresentAndValid = { key: String? ->
        !key.isNullOrBlank() && key != "YOUR_API_KEY" && key != "DEFAULT_API_KEY"
    }

    // Auto-populate secrets.properties from /usr/local/google/home/dkhawk/secrets.txt if needed
    val populateFromSystemSecrets = {
        if (dkhawkSecretsFile.exists()) {
            val systemProps = Properties()
            dkhawkSecretsFile.inputStream().use { systemProps.load(it) }

            val currentProps = Properties()
            if (secretsFile.exists()) {
                secretsFile.inputStream().use { currentProps.load(it) }
            }

            var updated = false
            for (key in requiredKeysToCheck + optionalKeysToCheck) {
                val sysVal = systemProps.getProperty(key)
                val curVal = currentProps.getProperty(key)
                if (!sysVal.isNullOrBlank() && !isValidApiKey(curVal) && !isPresentAndValid(curVal)) {
                    currentProps.setProperty(key, sysVal)
                    updated = true
                }
            }

            if (updated || !secretsFile.exists()) {
                secretsFile.outputStream().use { currentProps.store(it, "Auto-populated from /usr/local/google/home/dkhawk/secrets.txt") }
                println("Info: Auto-populated secrets.properties from /usr/local/google/home/dkhawk/secrets.txt")
            }
        }
    }

    // Run auto-population first
    populateFromSystemSecrets()

    val requestedTasks = gradle.startParameter.taskNames
    val buildOrInstallTasks = listOf("build", "install", "assemble", "package", "bundle")
    val isBuildOrInstallTarget = requestedTasks.any { taskName ->
        buildOrInstallTasks.any { kw -> taskName.contains(kw, ignoreCase = true) }
    }
    val isUnitTestOnly = requestedTasks.all { it.contains("test", ignoreCase = true) || it.contains("lint", ignoreCase = true) }

    if (isBuildOrInstallTarget && !isUnitTestOnly) {
        if (!secretsFile.exists()) {
            throw GradleException("FAILED BUILD: 'secrets.properties' file is missing! Building and installing the app requires valid API keys in secrets.properties or /usr/local/google/home/dkhawk/secrets.txt.")
        }

        val secrets = Properties()
        secretsFile.inputStream().use { secrets.load(it) }

        val missingOrInvalid = mutableListOf<String>()

        requiredKeysToCheck.forEach { reqKey ->
            val value = secrets.getProperty(reqKey)
            if (reqKey.endsWith("API_KEY", ignoreCase = true)) {
                if (!isValidApiKey(value)) {
                    missingOrInvalid.add("$reqKey (must be valid Google API key starting with 'AIza')")
                }
            } else {
                if (!isPresentAndValid(value)) {
                    missingOrInvalid.add(reqKey)
                }
            }
        }

        if (missingOrInvalid.isNotEmpty()) {
            throw GradleException(
                """
                ================================================================================
                BUILD CANCELLED: Missing or Placeholder API Keys Detected!
                --------------------------------------------------------------------------------
                The following required key(s) are missing or invalid in secrets.properties:
                ${missingOrInvalid.joinToString("\n") { " - $it" }}

                Please copy valid keys from /usr/local/google/home/dkhawk/secrets.txt or edit
                'secrets.properties' in the project root before building/installing the application.
                ================================================================================
                """.trimIndent()
            )
        }
    }
}

plugins {
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

secrets {
    defaultPropertiesFileName = "local.defaults.properties"
    propertiesFileName = "secrets.properties"
}

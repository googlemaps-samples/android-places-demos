/*
 * Copyright 2025 Google LLC
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

// The `plugins` block is where we apply Gradle plugins to this module.
// Plugins add new tasks and configurations to our build process.
plugins {
    // The core plugin for building an Android application. It provides tasks like `assembleDebug`, `installDebug`, etc.
    alias(libs.plugins.android.application)
    // This plugin enables Kotlin support in the Android project, allowing us to write code in Kotlin.
    alias(libs.plugins.kotlin.android)
    // This plugin from Google helps manage API keys and other secrets by reading them from a `secrets.properties`
    // file (which should be in .gitignore) and exposing them in the `BuildConfig` file at compile time.
    // This is crucial for keeping sensitive data out of version control.
    alias(libs.plugins.secrets.gradle.plugin)
    // This plugin provides the necessary integration for using Jetpack Compose with the Kotlin compiler.
    alias(libs.plugins.kotlin.compose)
}

// The `android` block is where we configure all the Android-specific build options.
android {
    // The `namespace` is a unique identifier for the app's generated R class. It's also used
    // as the default `applicationId` if not specified in `defaultConfig`.
    namespace = "com.example.placedetailscompose"
    // `compileSdk` specifies the Android API level the app is compiled against.
    // Using a recent version allows us to use the latest Android features.
    compileSdk = 36

    defaultConfig {
        // `applicationId` is the unique identifier for the app on the Google Play Store and on the device.
        applicationId = "com.example.placedetailscompose"
        // `minSdk` is the minimum API level required to run the app. Devices below this level cannot install it.
        minSdk = 27
        // `targetSdk` indicates the API level the app was tested against. Android may enable
        // compatibility behaviors on newer OS versions if the target is lower.
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // Specifies the instrumentation runner for running Android tests.
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        // The `release` block configures settings for the release build of the app.
        release {
            // `isMinifyEnabled` enables code shrinking with R8 to reduce the app's size.
            // It's disabled here for simplicity in a sample app, but highly recommended for production.
            isMinifyEnabled = false
            // `proguardFiles` specifies the files that define the R8 shrinking and obfuscation rules.
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        // Sets the Java language compatibility for the source code and compiled bytecode.
        // Using Java 17 is required for modern Android development.
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        // Configures Kotlin-specific compiler options.
        compilerOptions {
            // Sets the target JVM version for the compiled Kotlin code.
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    buildFeatures {
        // `compose` enables Jetpack Compose for the project.
        compose = true
        // `buildConfig` generates a `BuildConfig` class that contains constants from the build configuration,
        // such as the API key from the secrets plugin.
        buildConfig = true
        // `viewBinding` generates a binding class for each XML layout file.
        viewBinding = true
    }

    java {
        // Specifies the Java language version for the project's toolchain.
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }
    composeOptions {
        // Sets the version of the Kotlin compiler extension for Compose. This version must be
        // compatible with the Kotlin version used in the project.
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// The `dependencies` block is where we declare all the external libraries the app needs.
// These are fetched from repositories like Maven Central and Google's Maven repository.
dependencies {
    // --- Core AndroidX & UI Libraries ---
    // These are foundational libraries for building modern Android apps.
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.compose.material.icons.core)

    // --- Google Play Services ---
    // These are the essential libraries for this sample, providing Maps and Places functionality.
    implementation(libs.google.maps.services) // The core SDK for embedding Google Maps.
    implementation(libs.places) // The SDK for the Places UI Kit (PlaceDetails fragments).
    implementation(libs.play.services.location) // Needed for the FusedLocationProviderClient to get the device's location.
    implementation(libs.maps.compose)
    implementation(libs.maps.compose.widgets)
    implementation(libs.maps.utils.ktx)
    implementation(libs.material) // For Material Design components (used in XML layouts).

    // --- Jetpack Compose ---
    // These libraries are for building UIs with Jetpack Compose.
    implementation(libs.androidx.material3) // The latest Material Design components for Compose.
    implementation(platform(libs.androidx.compose.bom)) // The Compose Bill of Materials (BOM) ensures all Compose libraries use compatible versions.
    implementation(libs.androidx.ui.tooling.preview) // For displaying @Preview composables in Android Studio.
    implementation(libs.androidx.ui.viewbinding)
    implementation(libs.androidx.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling) // Provides tools for inspecting Compose UIs.

    // --- Testing Libraries ---
    // These libraries are for writing and running tests.
    // `testImplementation` is for local unit tests (running on the JVM).
    testImplementation(libs.junit)
    // `androidTestImplementation` is for instrumented tests (running on an Android device or emulator).
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core) // For UI testing with the View system.
    // AndroidX libraries for creating test rules and running tests.
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.runner)

    // --- Compose Testing ---
    // These are specific to testing Jetpack Compose UIs.
    androidTestImplementation(platform(libs.androidx.compose.bom)) // BOM for testing libraries.
    androidTestImplementation(libs.androidx.ui.test.junit4) // The main library for Compose UI tests.
    debugImplementation(libs.androidx.ui.test.manifest) // Provides a manifest for UI tests.
}

// This block configures the Secrets Gradle Plugin.
secrets {
    // Specifies a default properties file. This is useful for CI/CD environments where
    // you might not have a local `secrets.properties` file.
    defaultPropertiesFileName = "local.defaults.properties"
    // Specifies the local properties file where secret keys (like the Places API key) are stored.
    // This file should be added to .gitignore to prevent it from being committed to version control.
    propertiesFileName = "secrets.properties"
}
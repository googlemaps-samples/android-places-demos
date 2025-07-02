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

// The plugins block applies various Gradle plugins to the project.
plugins {
    // Core plugin for building an Android application.
    alias(libs.plugins.android.application)
    // Plugin for enabling Kotlin support in an Android project.
    alias(libs.plugins.kotlin.android)
    // A plugin from Google to manage API keys and other secrets, keeping them out of source control.
    // It makes keys available in the BuildConfig file.
    alias(libs.plugins.secrets.gradle.plugin)

    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.placedetailsuikit"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.placedetailsuikit"
        minSdk = 27
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        // Sets the Java version compatibility for the source and compiled bytecode.
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    buildFeatures {
        // Enables ViewBinding, a type-safe way to access views defined in XML layouts.
        viewBinding = true
        // Enables access to build-time constants from the code (e.g., API keys).
        // Enables Compose for the project.
        compose = true
        buildConfig = true
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }
    composeOptions {
        // Sets the Kotlin compiler extension version for Compose.
        // Ensure this version is compatible with your Kotlin and Compose versions.
        kotlinCompilerExtensionVersion = "1.5.1"
    }
}

// The dependencies block declares all the external libraries the app needs.
dependencies {
    // --- AndroidX Core & UI Libraries ---
    // Provides core Kotlin extensions for the Android framework.
    implementation(libs.androidx.core.ktx)
    // Provides backward compatibility for newer Android features on older API levels.
    implementation(libs.androidx.appcompat)
    // A library for using modern Material Design components.
    implementation(libs.material)
    // Core library for managing Activities, including `enableEdgeToEdge` and `registerForActivityResult`.
    implementation(libs.androidx.activity)
    // A flexible layout manager for creating responsive UIs. Used for activity_main.xml.
    implementation(libs.androidx.constraintlayout)
    // Provides Kotlin extensions for working with Fragments.
    implementation(libs.androidx.fragment.ktx)
    // Provides ViewModel, a class designed to store and manage UI-related data in a lifecycle conscious way.
    implementation(libs.androidx.lifecycle.viewmodel.ktx)


    // --- Google Play Services ---
    // The core SDK for embedding Google Maps in the application.
    implementation(libs.google.maps.services)
    // The SDK for accessing Google's rich database of place information.
    // This provides the `PlaceDetailsCompactFragment`.
    implementation(libs.places)
    // Provides access to location services, such as the FusedLocationProviderClient
    // used to get the device's last known location.
    implementation(libs.play.services.location)
    implementation(libs.androidx.ui.tooling.preview.android)

    // --- Testing Libraries ---
    // Standard library for writing local unit tests.
    testImplementation(libs.junit)
    // AndroidX library for writing instrumented tests that run on a device or emulator.
    androidTestImplementation(libs.androidx.junit)
    // AndroidX library for UI testing.
    androidTestImplementation(libs.androidx.espresso.core)
    // AndroidX libraries for creating test rules and running tests.
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.runner)

    // Compose
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))
}

// Configuration for the Secrets Gradle Plugin.
secrets {
    // Specifies a default properties file, useful for CI/CD environments.
    defaultPropertiesFileName = "local.defaults.properties"
    // Specifies the local properties file where secret keys are stored. This file should be in .gitignore.
    propertiesFileName = "secrets.properties"
}

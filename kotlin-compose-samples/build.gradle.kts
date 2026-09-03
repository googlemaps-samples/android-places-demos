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

plugins {
    id("places-demo.android.application")
    alias(libs.plugins.kotlin.android)
    id("places-demo.secrets")
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.google.android.libraries.places.samples.kotlincompose"

    defaultConfig {
        applicationId = "com.google.android.libraries.places.samples.kotlincompose"
        minSdk = 27
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

    kotlin {
        compilerOptions {
            freeCompilerArgs.addAll(
                "-opt-in=kotlin.RequiresOptIn",
                "-Xannotation-default-target=param-property"
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // --- Core AndroidX & Compose ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.fragment.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation("androidx.navigation:navigation-compose:2.8.8")

    // --- Google Services & Places SDK ---
    implementation(libs.google.maps.services)
    implementation(libs.places)
    implementation(libs.play.services.location)
    implementation(libs.maps.compose)
    implementation(libs.maps.compose.widgets)
    implementation(libs.maps.utils.ktx)
    implementation(libs.material)

    // --- Material 3 & Compose Infrastructure ---
    implementation(libs.androidx.material3)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.ui.viewbinding)
    implementation(libs.coil.compose)

    debugImplementation(libs.androidx.ui.tooling)

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

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

plugins {
    id("places-demo.android.application")
    alias(libs.plugins.jetbrains.kotlin.android)
    id("places-demo.secrets")
    alias(libs.plugins.jetbrains.kotlin.parcelize)
    kotlin("kapt")
}

android {
    namespace = "com.example.placesdemo"

    defaultConfig {
        applicationId = "com.example.placesdemo"
        versionCode = 1
        versionName = "1.0"

        multiDexEnabled = true
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    kotlin {
        compilerOptions {
            freeCompilerArgs.addAll(
                "-opt-in=kotlin.RequiresOptIn",
                "-Xannotation-default-target=param-property"
            )
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.core.ktx)
    implementation(libs.material)

    implementation(libs.volley)
    implementation(libs.glide)
    kapt(libs.glide.compiler)
    implementation(libs.viewbinding)
    implementation(libs.multidex)

    // Google Places
    implementation(libs.places)
    implementation(libs.maps.utils.ktx)
}


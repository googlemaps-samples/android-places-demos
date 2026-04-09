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
    alias(libs.plugins.jetbrains.kotlin.android)
    id("places-demo.secrets")
}

android {
    namespace = "com.google.places"

    defaultConfig {
        applicationId = "com.google.places"
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

demoApp {
    mainActivity.set(".kotlin.KotlinMainActivity")
}

// [START maps_android_places_install_snippet]
dependencies {
    // [START_EXCLUDE silent]
    implementation(libs.constraintlayout)
    implementation(libs.activity)
    implementation(libs.fragment)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.core.ktx)

    implementation(libs.appcompat)
    implementation(libs.material)

    implementation(libs.volley)
    implementation(libs.glide)
    implementation(libs.viewbinding)
    implementation(libs.multidex)
    // [END_EXCLUDE]

    // Places and Maps SDKs
    // [START maps_android_places_upgrade_snippet]
    implementation("com.google.android.libraries.places:places:5.2.0")
    // [END maps_android_places_upgrade_snippet]
}
// [END maps_android_places_install_snippet]



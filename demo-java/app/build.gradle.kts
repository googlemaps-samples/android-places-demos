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
    alias(libs.plugins.android.application)
    alias(libs.plugins.secrets.gradle.plugin)
}

android {
    namespace = "com.example.placesdemo"
    compileSdk = libs.versions.sdk.compile.get().toInt()

    defaultConfig {
        applicationId = "com.example.placesdemo"
        minSdk = libs.versions.sdk.min.get().toInt()
        targetSdk = libs.versions.sdk.target.get().toInt()
        versionCode = 1
        versionName = "1.0"

        multiDexEnabled = true
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.constraintlayout)
    implementation(libs.activity)
    implementation(libs.fragment)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    implementation(libs.appcompat)
    implementation(libs.material)

    implementation(libs.volley)
    implementation(libs.glide)
    implementation(libs.viewbinding)
    implementation(libs.multidex)

    // Places and Maps SDKs
    implementation(libs.places)
    implementation(libs.play.services.maps)
    implementation(libs.android.maps.utils)
}

// Secrets for Google Maps API Keys
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

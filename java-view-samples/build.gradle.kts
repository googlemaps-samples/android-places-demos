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
    id("places-demo.secrets")
}

android {
    namespace = "com.google.android.libraries.places.samples.javaview"

    defaultConfig {
        applicationId = "com.google.android.libraries.places.samples.javaview"
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.activity)
    implementation(libs.fragment)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    implementation(libs.places)
    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)

    implementation(libs.multidex)
    implementation(libs.glide)
}

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
    alias(libs.plugins.jetbrains.kotlin.parcelize)
}

android {
    namespace = "com.google.android.libraries.places.samples.kotlinview"

    defaultConfig {
        applicationId = "com.google.android.libraries.places.samples.kotlinview"
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
            freeCompilerArgs.addAll(
                "-opt-in=kotlin.RequiresOptIn",
                "-Xannotation-default-target=param-property",
                "-Xskip-metadata-version-check"
            )
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.core.ktx)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.viewbinding)
    implementation(libs.multidex)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment.ktx)

    // Google Places & Location
    implementation(libs.places)
    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    implementation(libs.maps.utils.ktx)

    // Image loading
    implementation(libs.coil.compose)
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.secrets.gradle.plugin)
    alias(libs.plugins.jetbrains.kotlin.parcelize)
}

android {
    namespace = "com.example.placesuikit3d"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.placesuikit3d"
        minSdk = 29
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
        // `viewBinding` generates a binding class for each XML layout file, providing a type-safe
        // way to access views without `findViewById`. This is used in the XML-based activities.
        viewBinding = true
        // `compose` enables Jetpack Compose for the project.
        compose = true
        // `buildConfig` generates a `BuildConfig` class that contains constants from the build configuration,
        // such as the API key from the secrets plugin.
        buildConfig = true
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.fragment.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.kotlinx.datetime)
    implementation(libs.dagger)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.android)

    implementation(libs.play.services.maps3d)

    testImplementation(libs.google.truth)

    // Google Maps Utils for the polyline decoder
    implementation(libs.maps.utils.ktx)

    implementation(libs.androidx.material.icons.extended)

    // Google Places
    implementation(libs.places)
    implementation(libs.androidx.fragment.ktx)

    // You must include this dependency to use the Places SDK.
    implementation(libs.material)
}

secrets {
    // Optionally specify a different file name containing your secrets.
    // The plugin defaults to "local.properties"
    propertiesFileName = "secrets.properties"

    // A properties file containing default secret values. This file can be
    // checked in version control.
    defaultPropertiesFileName = "local.defaults.properties"
}

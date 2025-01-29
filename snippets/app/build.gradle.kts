plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.secrets.gradle.plugin)
}

android {
    namespace = "com.google.places"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.google.places"
        minSdk = 23
        targetSdk = 35
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
        buildConfig = true
    }
}

// [START maps_android_places_install_snippet]
dependencies {
    // [START_EXCLUDE silent]
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
    // [END_EXCLUDE]

    // Places and Maps SDKs
    implementation("com.google.android.libraries.places:places:4.1.0")
}
// [END maps_android_places_install_snippet]

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

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.secrets.gradle.plugin)
    alias(libs.plugins.jetbrains.kotlin.parcelize)
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.multidex)

    implementation(libs.volley)
    implementation(libs.glide)
    implementation(libs.viewbinding)

    implementation(libs.activityKtx)

    implementation(libs.google.maps.services)

    // Google Places
    implementation(libs.places)
    implementation(libs.maps.utils.ktx)

    implementation(libs.androidx.fragment.ktx)
    implementation(libs.play.services.location)
}

secrets {
    defaultPropertiesFileName = "local.defaults.properties"
    propertiesFileName = "secrets.properties"
}

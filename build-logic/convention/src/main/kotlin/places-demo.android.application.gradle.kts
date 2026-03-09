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
    id("com.android.application")
}

interface DemoAppExtension {
    val mainActivity: Property<String>
}

val demoApp = extensions.create<DemoAppExtension>("demoApp")
demoApp.mainActivity.convention(".MainActivity")

android {
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        targetSdk = 36
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    buildFeatures {
        buildConfig = true
    }
}

afterEvaluate {
    val androidExt = project.extensions.getByType(com.android.build.api.dsl.ApplicationExtension::class.java)
    val appId = androidExt.defaultConfig.applicationId ?: androidExt.namespace ?: project.name
    val namespace = androidExt.namespace ?: appId
    val mainAct = demoApp.mainActivity.get()
    val componentName = if (mainAct.startsWith(".")) "$appId/$namespace$mainAct" else "$appId/$mainAct"

    val androidComponents = project.extensions.getByType(com.android.build.api.variant.AndroidComponentsExtension::class.java)
    val adbPath = androidComponents.sdkComponents.adb.get().asFile.absolutePath

    tasks.register<Exec>("installAndLaunch") {
        description = "Installs the debug APK and launches the main activity."
        group = "application"
        dependsOn("installDebug")
        commandLine(adbPath, "shell", "am", "start", "-n", componentName)
    }
}

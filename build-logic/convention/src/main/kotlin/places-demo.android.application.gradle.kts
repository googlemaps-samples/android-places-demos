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

import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("com.android.application")
}

interface DemoAppExtension {
    val mainActivity: Property<String>
}

val demoApp = extensions.create<DemoAppExtension>("demoApp")
demoApp.mainActivity.convention(".MainActivity")

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
val demoCompileSdk = libs.findVersion("compileSdk").get().requiredVersion.toInt()
val demoMinSdk = libs.findVersion("minSdk").get().requiredVersion.toInt()
val demoTargetSdk = libs.findVersion("targetSdk").map { it.requiredVersion.toInt() }.orElse(demoCompileSdk)

android {
    compileSdk = demoCompileSdk

    defaultConfig {
        minSdk = demoMinSdk
        targetSdk = demoTargetSdk
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

    val requestedActivity = project.findProperty("activity")?.toString()?.trim()
    val requestedSample = project.findProperty("sample")?.toString()?.trim()
    val requestedFramework = project.findProperty("framework")?.toString()?.trim()

    val targetAct = if (!requestedActivity.isNullOrEmpty()) requestedActivity else mainAct
    val componentName = when {
        targetAct.startsWith(".") -> "$appId/$namespace$targetAct"
        targetAct.contains(".") -> "$appId/$targetAct"
        else -> "$appId/$namespace.$targetAct"
    }

    val androidComponents = project.extensions.getByType(com.android.build.api.variant.AndroidComponentsExtension::class.java)
    val adbPath = androidComponents.sdkComponents.adb.get().asFile.absolutePath

    tasks.register<Exec>("installAndLaunch") {
        description = "Installs the debug APK and launches the catalog or a specific sample (-Psample=... -Pframework=... or -Pactivity=...)."
        group = "application"
        dependsOn("installDebug")

        val argsList = mutableListOf(adbPath, "shell", "am", "start", "-n", componentName)
        if (!requestedSample.isNullOrEmpty()) {
            argsList.addAll(listOf("--es", "extra_launch_sample", requestedSample))
        }
        if (!requestedFramework.isNullOrEmpty()) {
            argsList.addAll(listOf("--es", "extra_framework", requestedFramework))
        }
        commandLine(argsList)
    }
}

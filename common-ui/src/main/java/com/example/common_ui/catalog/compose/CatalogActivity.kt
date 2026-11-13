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

package com.example.common_ui.catalog.compose

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.common_ui.catalog.Framework
import com.example.common_ui.catalog.SampleCatalogRegistry
import com.example.common_ui.catalog.SampleItem
import com.example.common_ui.catalog.ui.UnifiedCatalogActivity

/**
 * Clean, developer-focused Sample Catalog Activity built with Jetpack Compose.
 *
 * Provides fast searching, complexity and hashtag filtering, and cross-framework sample launching
 * across Kotlin Views, Java Views, and Jetpack Compose.
 *
 * Also supports direct CLI / ADB auto-launching of any sample via intent extras:
 * - `extra_launch_sample`: sample ID, activity class name, or title
 * - `extra_framework`: optional framework (`KOTLIN_VIEWS`, `JAVA_VIEWS`, `KOTLIN_COMPOSE`, or shorthand `kotlin`, `java`, `compose`)
 */
open class CatalogActivity : ComponentActivity() {

    /**
     * Subclasses in specific modules can override the default selected framework tab
     * (e.g., Java Views in `:demo-java` or Jetpack Compose in `:PlaceDetailsCompose`).
     */
    protected open val defaultFramework: Framework = Framework.KOTLIN_VIEWS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialFramework = parseFrameworkExtra(intent?.getStringExtra(EXTRA_FRAMEWORK))
            ?: defaultFramework

        setContent {
            CatalogTheme {
                CatalogScreen(
                    isReviewerMode = false,
                    evaluations = emptyMap(),
                    initialFramework = initialFramework,
                    onLaunchSample = { sample, framework ->
                        launchSample(sample, framework)
                    },
                    onSwitchMode = {
                        try {
                            val intent = Intent().apply {
                                setClassName(packageName, "com.example.reviewer.compose.ReviewerActivity")
                            }
                            startActivity(intent)
                            finish()
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@CatalogActivity,
                                "Reviewer mode not available in this build",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    },
                )
            }
        }

        if (savedInstanceState == null) {
            handleAutoLaunchIntent(intent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAutoLaunchIntent(intent)
    }

    private fun handleAutoLaunchIntent(intent: Intent?) {
        val sampleQuery = intent?.getStringExtra(EXTRA_LAUNCH_SAMPLE)
            ?.takeIf { it.isNotBlank() } ?: return
        val requestedFramework = parseFrameworkExtra(intent.getStringExtra(EXTRA_FRAMEWORK))
        val sample = SampleCatalogRegistry.findById(sampleQuery)
        if (sample != null) {
            val targetFramework = when {
                requestedFramework != null && sample.getActivityForFramework(requestedFramework) != null ->
                    requestedFramework

                sample.getActivityForFramework(defaultFramework) != null ->
                    defaultFramework

                sample.kotlinActivity != null -> Framework.KOTLIN_VIEWS

                sample.composeActivity != null -> Framework.KOTLIN_COMPOSE

                sample.javaActivity != null -> Framework.JAVA_VIEWS

                else -> defaultFramework
            }
            launchSample(sample, targetFramework)
        } else {
            Toast.makeText(
                this,
                "Sample not found in catalog: $sampleQuery",
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    protected fun parseFrameworkExtra(raw: String?): Framework? {
        if (raw.isNullOrBlank()) return null
        return when (raw.trim().lowercase()) {
            "kotlin", "kotlin_views", "kotlin-views", "views" -> Framework.KOTLIN_VIEWS
            "java", "java_views", "java-views" -> Framework.JAVA_VIEWS
            "compose", "kotlin_compose", "kotlin-compose", "jetpack_compose" -> Framework.KOTLIN_COMPOSE
            else -> Framework.entries.firstOrNull { it.name.equals(raw.trim(), ignoreCase = true) }
        }
    }

    protected fun launchSample(sample: SampleItem, framework: Framework) {
        val className = sample.getActivityForFramework(framework)
        if (className.isNullOrBlank()) {
            Toast.makeText(
                this,
                "No ${framework.displayName} implementation available for ${sample.title}",
                Toast.LENGTH_SHORT,
            ).show()
            return
        }

        val targetPackage = SampleItem.resolveTargetPackage(
            activityClassName = className,
            framework = framework,
            fallbackPackage = packageName,
        )

        val launchIntent = Intent().apply {
            setClassName(targetPackage, className)
            putExtra(UnifiedCatalogActivity.EXTRA_SAMPLE_ID, sample.id)
            putExtra("extra_sample_id", sample.id)
            putExtra("extra_is_reviewer_mode", false)
        }

        try {
            if (launchIntent.resolveActivity(packageManager) != null) {
                startActivity(launchIntent)
            } else {
                Toast.makeText(
                    this,
                    "${framework.displayName} app ($targetPackage) is not installed.\nRun ./gradlew installAllDebug to install all modules.",
                    Toast.LENGTH_LONG,
                ).show()
            }
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                this,
                "${framework.displayName} app ($targetPackage) is not installed.\nRun ./gradlew installAllDebug to install all modules.",
                Toast.LENGTH_LONG,
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Could not launch ${sample.title}: ${e.message}",
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    companion object {
        const val EXTRA_LAUNCH_SAMPLE = "extra_launch_sample"
        const val EXTRA_FRAMEWORK = "extra_framework"
    }
}

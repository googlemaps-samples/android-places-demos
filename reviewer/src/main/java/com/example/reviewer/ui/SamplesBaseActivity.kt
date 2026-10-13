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

package com.example.reviewer.ui

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.common_ui.R
import com.example.common_ui.catalog.Framework
import com.example.common_ui.catalog.ReviewStatus
import com.example.common_ui.catalog.Sample
import com.example.common_ui.catalog.SampleCatalogRegistry
import com.example.common_ui.catalog.SampleItem
import com.example.common_ui.catalog.toSampleItem
import com.example.common_ui.catalog.ui.UnifiedCatalogActivity
import com.example.reviewer.repository.SampleReviewRepository
import kotlinx.coroutines.launch

/**
 * Base activity for View-based sample activities that provides:
 * - Edge-to-edge system bar insets handling
 * - Action bar options menu for "About & APIs" ([SampleExpectationsBottomSheet]),
 *   cross-framework switching (Kotlin Views / Java Views / Jetpack Compose),
 *   and Reviewer Mode grading controls ([ReviewEvaluationDialog]).
 * - Back navigation to the Unified Sample Catalog when launched directly as a task root.
 */
open class SamplesBaseActivity : AppCompatActivity() {

    protected open val activityFramework: Framework = Framework.KOTLIN_VIEWS
    protected var currentSampleMetadata: SampleItem? = null
    protected val reviewRepository: SampleReviewRepository by lazy {
        SampleReviewRepository.getInstance(this)
    }

    protected val isReviewerMode: Boolean
        get() = intent.getBooleanExtra("extra_is_reviewer_mode", false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        val isLightTheme = resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_NO
        insetsController.isAppearanceLightStatusBars = isLightTheme

        val contentView = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(contentView) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        resolveSampleMetadata()
        onBackPressedDispatcher.addCallback(this) {
            navigateBackToCatalog()
        }
    }

    private fun resolveSampleMetadata() {
        val sampleId = intent.getStringExtra(UnifiedCatalogActivity.EXTRA_SAMPLE_ID)
        currentSampleMetadata = SampleCatalogRegistry.findById(sampleId)
            ?: SampleCatalogRegistry.findById(this::class.java.name)
            ?: this::class.java.getAnnotation(Sample::class.java)?.toSampleItem(this::class.java.name)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        val metadata = currentSampleMetadata ?: return true

        if (isReviewerMode) {
            menu.add(0, MENU_PASS, 0, "Good Job (Pass)")
                .setIcon(R.drawable.ic_thumb_up)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

            menu.add(0, MENU_NEEDS_WORK, 1, "Something's Wrong")
                .setIcon(R.drawable.ic_warning_bug)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

            menu.add(0, MENU_NEXT_UNCHECKED, 2, "Next Unchecked")
                .setIcon(R.drawable.ic_skip_next)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

            menu.add(0, MENU_INFO, 3, "Criteria & Purpose")
                .setIcon(R.drawable.ic_info_outline)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)

            menu.add(0, MENU_PREV_SAMPLE, 4, "Previous Sample")
                .setIcon(R.drawable.ic_skip_previous)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)

            menu.add(0, MENU_RESET_STATUS, 5, "Reset to Unchecked")
                .setIcon(R.drawable.ic_undo)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        } else {
            menu.add(0, MENU_INFO, 0, "About & APIs")
                .setIcon(R.drawable.ic_info_outline)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        }

        val altFrameworks = metadata.getAlternativeFrameworks(activityFramework)
        altFrameworks.forEachIndexed { idx, altFw ->
            menu.add(0, MENU_SWITCH_FRAMEWORK_BASE + altFw.ordinal, 10 + idx, "Switch to ${altFw.displayName}")
                .setIcon(R.drawable.ic_swap_framework)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val metadata = currentSampleMetadata
        return when (item.itemId) {
            android.R.id.home -> {
                navigateBackToCatalog()
                true
            }

            MENU_INFO -> {
                if (metadata != null) {
                    val sheet = SampleExpectationsBottomSheet.newInstance(
                        sample = metadata,
                        framework = activityFramework,
                        isReviewerMode = isReviewerMode,
                        onLaunch = { s, fw ->
                            SampleReviewRepository.launchSample(this@SamplesBaseActivity, s, fw, isReviewerMode)
                        },
                    )
                    sheet.show(supportFragmentManager, "SampleExpectationsBottomSheet")
                }
                true
            }

            MENU_PASS -> {
                if (metadata != null) {
                    ReviewEvaluationDialog.show(this, metadata, activityFramework, ReviewStatus.PASSING)
                }
                true
            }

            MENU_NEEDS_WORK -> {
                if (metadata != null) {
                    ReviewEvaluationDialog.show(this, metadata, activityFramework, ReviewStatus.NEEDS_WORK)
                }
                true
            }

            MENU_NEXT_UNCHECKED -> {
                if (metadata != null) {
                    lifecycleScope.launch {
                        val next = reviewRepository.getNextUncheckedSample(metadata.id, activityFramework)
                        if (next != null) {
                            SampleReviewRepository.launchSample(this@SamplesBaseActivity, next, activityFramework)
                        } else {
                            Toast.makeText(
                                this@SamplesBaseActivity,
                                "🎉 All ${activityFramework.displayName} samples reviewed!",
                                Toast.LENGTH_LONG,
                            ).show()
                            navigateBackToCatalog()
                        }
                    }
                }
                true
            }

            MENU_PREV_SAMPLE -> {
                if (metadata != null) {
                    lifecycleScope.launch {
                        val prev = reviewRepository.getPreviousSample(metadata.id, activityFramework)
                        if (prev != null) {
                            SampleReviewRepository.launchSample(this@SamplesBaseActivity, prev, activityFramework)
                        }
                    }
                }
                true
            }

            MENU_RESET_STATUS -> {
                if (metadata != null) {
                    val targetFqcn = metadata.getTargetFqcn(activityFramework)
                    reviewRepository.deleteEvaluation(targetFqcn) {
                        Toast.makeText(this, "Reverted ${metadata.title} to Unchecked", Toast.LENGTH_SHORT).show()
                    }
                }
                true
            }

            in MENU_SWITCH_FRAMEWORK_BASE until (MENU_SWITCH_FRAMEWORK_BASE + Framework.entries.size) -> {
                val targetOrdinal = item.itemId - MENU_SWITCH_FRAMEWORK_BASE
                val targetFw = Framework.entries.getOrNull(targetOrdinal)
                if (metadata != null && targetFw != null) {
                    SampleReviewRepository.launchSample(this, metadata, targetFw, isReviewerMode)
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    protected fun navigateBackToCatalog() {
        if (isTaskRoot) {
            try {
                val targetActivity = if (isReviewerMode) {
                    "com.example.reviewer.compose.ReviewerActivity"
                } else {
                    "com.example.common_ui.catalog.compose.CatalogActivity"
                }
                val intent = Intent().setClassName(packageName, targetActivity)
                startActivity(intent)
            } catch (e: Exception) {
                // Fallback
            }
        }
        finish()
    }

    companion object {
        private const val MENU_INFO = 2001
        private const val MENU_PASS = 2003
        private const val MENU_NEEDS_WORK = 2004
        private const val MENU_NEXT_UNCHECKED = 2005
        private const val MENU_PREV_SAMPLE = 2006
        private const val MENU_RESET_STATUS = 2007
        private const val MENU_SWITCH_FRAMEWORK_BASE = 2010
    }
}

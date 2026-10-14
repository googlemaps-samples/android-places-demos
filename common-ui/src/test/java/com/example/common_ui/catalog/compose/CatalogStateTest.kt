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

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarHostState
import com.example.common_ui.catalog.Framework
import com.example.common_ui.catalog.ReviewStatus
import com.example.common_ui.catalog.SampleCatalogRegistry
import com.example.common_ui.catalog.SampleEvaluation
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.TestScope
import org.junit.Test

/**
 * Unit tests for [CatalogState] across all 3 frameworks.
 */
class CatalogStateTest {

    @Test
    fun statusCountsAndNextUnchecked_isolatedPerFramework() {
        val sample = SampleCatalogRegistry.findById("PlaceAutocompleteActivity")!!
        val kotlinFqcn = sample.getTargetFqcn(Framework.KOTLIN_VIEWS)
        val composeFqcn = sample.getTargetFqcn(Framework.KOTLIN_COMPOSE)

        val evaluations = mapOf(
            kotlinFqcn to SampleEvaluation(
                sampleId = kotlinFqcn,
                sampleTitle = sample.title,
                activityName = kotlinFqcn,
                category = sample.category,
                framework = Framework.KOTLIN_VIEWS.name,
                status = ReviewStatus.PASSING.name,
                notes = "Works great",
            ),
        )

        val state = CatalogState(
            isReviewerMode = true,
            evaluations = evaluations,
            lazyListState = LazyListState(),
            snackbarHostState = SnackbarHostState(),
            coroutineScope = TestScope(),
            initialFramework = Framework.KOTLIN_VIEWS,
        )

        assertThat(state.passingCount).isEqualTo(1)

        // Switching to Compose tab should show 0 passing because evaluation was on Kotlin Views FQCN
        state.selectedFramework = Framework.KOTLIN_COMPOSE
        assertThat(state.passingCount).isEqualTo(0)
        assertThat(state.uncheckedCount).isEqualTo(state.frameworkSamples.size)
        assertThat(composeFqcn).isNotEqualTo(kotlinFqcn)
    }
}

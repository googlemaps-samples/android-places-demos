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

package com.example.common_ui.catalog

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests verifying [SampleCatalogRegistry] completeness, 3-framework filtering,
 * CLI / Android Studio sample lookup ([SampleCatalogRegistry.findById]), and cross-app
 * package resolution ([SampleItem.resolveTargetPackage]).
 */
class SampleCatalogRegistryTest {

    @Test
    fun allRegisteredSamples_haveCompleteMetadataAndApiCalls() {
        assertThat(SampleCatalogRegistry.SAMPLES).isNotEmpty()
        val ids = mutableSetOf<String>()

        for (sample in SampleCatalogRegistry.SAMPLES) {
            assertThat(sample.id).isNotEmpty()
            assertThat(ids.add(sample.id)).isTrue()
            assertThat(sample.title).isNotEmpty()
            assertThat(sample.category).isNotEmpty()
            assertThat(sample.description).isNotEmpty()
            assertThat(sample.purpose).isNotEmpty()
            assertThat(sample.successCriteria).isNotEmpty()
            assertThat(sample.tags).isNotEmpty()
            assertThat(sample.apiCalls).isNotEmpty()

            // Every sample must implement at least one framework
            val implementedFrameworks = Framework.entries.filter {
                sample.getActivityForFramework(it) != null
            }
            assertThat(implementedFrameworks).isNotEmpty()
        }
    }

    @Test
    fun filter_byThreeFrameworks_returnsExpectedSubsets() {
        val kotlinViews = SampleCatalogRegistry.filter(framework = Framework.KOTLIN_VIEWS)
        val javaViews = SampleCatalogRegistry.filter(framework = Framework.JAVA_VIEWS)
        val compose = SampleCatalogRegistry.filter(framework = Framework.KOTLIN_COMPOSE)

        assertThat(kotlinViews).isNotEmpty()
        assertThat(javaViews).isNotEmpty()
        assertThat(compose).isNotEmpty()

        assertThat(kotlinViews.all { it.kotlinActivity != null }).isTrue()
        assertThat(javaViews.all { it.javaActivity != null }).isTrue()
        assertThat(compose.all { it.composeActivity != null }).isTrue()
    }

    @Test
    fun filter_byComplexity_includesSnippetsSimpleAndAdvanced() {
        val snippets = SampleCatalogRegistry.filter(
            framework = Framework.KOTLIN_VIEWS,
            complexity = Complexity.SNIPPET,
        )
        val simple = SampleCatalogRegistry.filter(
            framework = Framework.KOTLIN_VIEWS,
            complexity = Complexity.SIMPLE,
        )
        val advanced = SampleCatalogRegistry.filter(
            framework = Framework.KOTLIN_VIEWS,
            complexity = Complexity.ADVANCED,
        )

        assertThat(snippets).hasSize(7)
        assertThat(simple).isNotEmpty()
        assertThat(advanced).isNotEmpty()
    }

    @Test
    fun findById_resolvesByFqcnSimpleClassNameAndTitle() {
        // 1. Exact FQCN
        val byFqcn = SampleCatalogRegistry.findById("com.example.placesdemo.PlaceAutocompleteActivity")
        assertThat(byFqcn).isNotNull()
        assertThat(byFqcn?.title).isEqualTo("Place Autocomplete")

        // 2. Simple class name (CLI -Psample=PlaceAutocompleteActivity)
        val bySimpleName = SampleCatalogRegistry.findById("PlaceAutocompleteActivity")
        assertThat(bySimpleName).isEqualTo(byFqcn)

        // 3. Compose counterpart class name (CLI -Psample=PlacesSearchDemoActivity)
        val byComposeClass = SampleCatalogRegistry.findById("PlacesSearchDemoActivity")
        assertThat(byComposeClass).isNotNull()
        assertThat(byComposeClass?.composeActivity)
            .isEqualTo("com.google.places.android.ktx.demo.PlacesSearchDemoActivity")

        // 4. Java counterpart class name (CLI -Psample=ProgrammaticAutocompleteToolbarActivity)
        val byJavaClass = SampleCatalogRegistry.findById("ProgrammaticAutocompleteToolbarActivity")
        assertThat(byJavaClass).isNotNull()
        assertThat(byJavaClass?.javaActivity)
            .isEqualTo("com.example.placesdemo.programmatic_autocomplete.ProgrammaticAutocompleteToolbarActivity")

        // 5. Text & Nearby Search across all 3 frameworks (CLI -Psample=TextAndNearbySearchActivity or PlacesTextAndNearbySearchDemoActivity)
        val byTextSearch = SampleCatalogRegistry.findById("TextAndNearbySearchActivity")
        assertThat(byTextSearch).isNotNull()
        assertThat(byTextSearch?.kotlinActivity).isEqualTo("com.example.placesdemo.TextAndNearbySearchActivity")
        assertThat(byTextSearch?.javaActivity).isEqualTo("com.example.placesdemo.TextAndNearbySearchActivity")
        assertThat(byTextSearch?.composeActivity)
            .isEqualTo("com.google.places.android.ktx.demo.PlacesTextAndNearbySearchDemoActivity")
        assertThat(SampleCatalogRegistry.findById("PlacesTextAndNearbySearchDemoActivity")).isEqualTo(byTextSearch)
    }

    @Test
    fun resolveTargetPackage_mapsAllSevenModulesCorrectly() {
        // 1. demo-kotlin vs demo-java share the com.example.placesdemo.* namespace
        assertThat(
            SampleItem.resolveTargetPackage(
                "com.example.placesdemo.PlaceAutocompleteActivity",
                Framework.KOTLIN_VIEWS,
                "fallback",
            ),
        ).isEqualTo("com.example.placesdemo")

        assertThat(
            SampleItem.resolveTargetPackage(
                "com.example.placesdemo.PlaceAutocompleteActivity",
                Framework.JAVA_VIEWS,
                "fallback",
            ),
        ).isEqualTo("com.example.placesdemo.java")

        // 2. PlaceDetailsUIKit
        assertThat(
            SampleItem.resolveTargetPackage(
                "com.example.placedetailsuikit.compact.ConfigurablePlaceDetailsActivity",
                Framework.KOTLIN_VIEWS,
                "fallback",
            ),
        ).isEqualTo("com.example.placedetailsuikit")

        // 3. kotlin-demos (namespace com.google.places.android.ktx.demo -> appId com.google.maps.android.ktx.demo)
        assertThat(
            SampleItem.resolveTargetPackage(
                "com.google.places.android.ktx.demo.AutocompleteDemoActivity",
                Framework.KOTLIN_COMPOSE,
                "fallback",
            ),
        ).isEqualTo("com.google.maps.android.ktx.demo")

        // 4. PlaceDetailsCompose
        assertThat(
            SampleItem.resolveTargetPackage(
                "com.example.placedetailscompose.MainActivity",
                Framework.KOTLIN_COMPOSE,
                "fallback",
            ),
        ).isEqualTo("com.example.placedetailscompose")

        // 5. PlacesUIKit3D
        assertThat(
            SampleItem.resolveTargetPackage(
                "com.example.placesuikit3d.MainActivity",
                Framework.KOTLIN_COMPOSE,
                "fallback",
            ),
        ).isEqualTo("com.example.placesuikit3d")

        // 6. snippets
        assertThat(
            SampleItem.resolveTargetPackage(
                "com.google.places.kotlin.PlaceDetailsActivity",
                Framework.KOTLIN_VIEWS,
                "fallback",
            ),
        ).isEqualTo("com.google.places")
    }

    @Sample(
        id = "com.example.placesdemo.AnnotatedTestActivity",
        title = "Annotated Test Sample",
        description = "Verifies @Sample annotation conversion to SampleItem.",
        category = "Place Autocomplete",
        complexity = Complexity.SIMPLE,
        tags = ["#autocomplete", "#test"],
        apiCalls = ["PlaceAutocomplete.IntentBuilder().build(context)"],
        purpose = "Tests annotation-based metadata resolution.",
        successCriteria = "Annotation converts cleanly to SampleItem.",
        failureIndicators = "Missing fields.",
        framework = Framework.KOTLIN_COMPOSE,
    )
    private class AnnotatedTestActivity

    @Test
    fun sampleAnnotation_toSampleItem_populatesAllFieldsAndFrameworkTarget() {
        val annotation = AnnotatedTestActivity::class.java.getAnnotation(Sample::class.java)
        assertThat(annotation).isNotNull()

        val item = annotation!!.toSampleItem(AnnotatedTestActivity::class.java.name)
        assertThat(item.id).isEqualTo("com.example.placesdemo.AnnotatedTestActivity")
        assertThat(item.title).isEqualTo("Annotated Test Sample")
        assertThat(item.complexity).isEqualTo(Complexity.SIMPLE)
        assertThat(item.tags).containsExactly("#autocomplete", "#test")
        assertThat(item.apiCalls).containsExactly("PlaceAutocomplete.IntentBuilder().build(context)")
        assertThat(item.composeActivity).isEqualTo(AnnotatedTestActivity::class.java.name)
        assertThat(item.kotlinActivity).isNull()
        assertThat(item.javaActivity).isNull()
    }
}

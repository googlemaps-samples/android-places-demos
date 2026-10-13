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

package com.example.placesdemo

import com.example.common_ui.catalog.Framework
import com.example.reviewer.ui.SamplesBaseActivity

/**
 * Base activity for Kotlin Views sample activities in `:demo-kotlin`.
 *
 * Inherits edge-to-edge system bar handling, "About & APIs" metadata sheet,
 * cross-framework switching (Kotlin Views / Java Views / Jetpack Compose),
 * and Reviewer Mode grading controls from [SamplesBaseActivity].
 */
open class BaseActivity : SamplesBaseActivity() {
    override val activityFramework: Framework = Framework.KOTLIN_VIEWS
}

# Place Details Compose Sample

This sample demonstrates how to integrate the **Places UI Kit** (specifically `PlaceDetailsCompactFragment` and `PlaceDetailsFragment`) into a **Jetpack Compose** application.

It showcases how to wrap these View-based fragments using `AndroidView` to create seamless Composable wrappers: `PlaceDetailsCompactView` and `PlaceDetailsFullView`.

## Features

- **Jetpack Compose Integration**: Demonstrates the `AndroidView` pattern for embedding Places UI Kit fragments.
- **Compact & Full Views**: Supports both the Compact (bottom sheet style) and Full (fullscreen style) variants of the UI Kit.
- **Dynamic Toggling**: Users can switch between Compact and Full views at runtime using a toggle switch.
- **Google Maps Integration**: Uses the Maps Compose library to display an interactive map.
- **MVVM Architecture**: Manages state (selected place, view mode) using a `MapViewModel`.
- **Secrets Management**: Securely handles API keys using the Secrets Gradle Plugin.

## Getting Started

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/googlemaps-samples/android-places-demos.git
    ```
2.  **Open in Android Studio:** Open the `PlaceDetailsCompose` directory.
3.  **Add API Key:**
    -   Create `secrets.properties` in the project root.
    -   Add your key: `PLACES_API_KEY="YOUR_API_KEY"` (ensure Places API and Maps SDK are enabled).
4.  **Run:** Build and run on a device/emulator.

## Code Highlights

### 1. Wrapping Fragments in Compose (`PlaceDetailsView.kt`)

The core of this integration is wrapping the `PlaceDetailsCompactFragment` and `PlaceDetailsFragment` in a Composable. We use `AndroidView` to host a `FragmentContainerView`.

**Key Steps:**
-   **Unique ID**: Generate a unique view ID (`View.generateViewId()`) for the container so `FragmentManager` can identify it.
-   **Fragment Management**: In the `update` block, check if the fragment exists. If not, create and add it.
-   **Safe Loading**: Use `view.post { ... }` to call `loadWithPlaceId`. This ensures the fragment's view is fully attached before data loading begins, preventing crashes.

```kotlin
@Composable
fun PlaceDetailsCompactView(place: PointOfInterest, ...) {
    val fragmentContainerId = remember { View.generateViewId() }
    
    AndroidView(
        factory = { context ->
            FragmentContainerView(context).apply { id = fragmentContainerId }
        },
        update = { view ->
            // ... Fragment transaction logic ...
            view.post { fragment.loadWithPlaceId(place.placeId) }
        }
    )
}
```

### 2. Switching Views (`MapScreen.kt`)

The app demonstrates how to dynamically switch between the Compact and Full views while maintaining the selected place context.

```kotlin
var isFullView by remember { mutableStateOf(false) }

if (isFullView) {
    PlaceDetailsFullView(place = place, ...)
} else {
    PlaceDetailsCompactView(place = place, ...)
}
```

### 3. Handling Events

We use `PlaceLoadListener` attached to the fragment to listen for success/failure events. These are propagated back to the Compose layer via callbacks (e.g., `onDismiss`).

## License

```
Copyright 2025 Google LLC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

# Architecture

This sample application demonstrates a modern **MVVM (Model-View-ViewModel)** architecture integrated with **Jetpack Compose** and **Google Maps Platform**.

## Overview

The app is a single-screen application (`MapScreen`) that allows users to view a map, track their location, and select places to view detailed information using the Google Places UI Kit.

### Key Layers

1.  **UI Layer (Compose)**:
    -   **`MapScreen`**: The main entry point. Handles the Scaffold, Map rendering (via Maps Compose), and UI controls.
    -   **`PlaceDetailsView`**: Bridging components (`PlaceDetailsCompactView`, `PlaceDetailsFullView`) that wrap the View-based Places fragments using `AndroidView`.
    -   **`MapViewModel`**: Holds the UI state (`selectedPlace`, `isFullView`, `deviceLocation`) and handles business logic.

2.  **Data Layer (Repository)**:
    -   **`LocationRepository`**: A light wrapper around `FusedLocationProviderClient`. It exposes location updates as a Kotlin `Flow`, handling permission checks gracefully.

## Key Patterns

### 1. Compose Interoperability (`AndroidView`)
The Google Places UI Kit components (`PlaceDetailsCompactFragment`, `PlaceDetailsFragment`) are standard Android Fragments. To use them in a pure Compose app, we use the `AndroidView` composable to host a `FragmentContainerView`.

-   **Unique IDs**: We generate unique View IDs using `View.generateViewId()` so the `FragmentManager` can correctly identify each container.
-   **Lifecycle Handling**: Fragments are added/removed via transactions inside the `AndroidView` factory.
-   **Updates**: Data updates (like changing the Place ID) are posted to the view queue (`view.post`) to ensure the Fragment is fully attached before loading data.

### 2. State Management with Flows
The `MapViewModel` uses `StateFlow` to expose reactive state to the UI.
-   **`flatMapLatest`**: Used for location updates to automatically switch between "no location" and "location updates" based on permission state.
-   **`combine` / `map`**: derived state is computed reactively.

## Common Integration Challenges

When integrating the Places UI Kit (View-based) into a Jetpack Compose app, there are a few specific implementation details to be aware of:

1.  **"Context must be a FragmentActivity" Crash**:
    -   *Why it happens*: The Places UI Kit fragments (`PlaceDetailsFragment`) rely on the legacy Android `FragmentManager`. This manager is only available in a `FragmentActivity` (or `AppCompatActivity`).
    -   *The Fix*: We assume the Composable is hosted in an Activity that extends `AppCompatActivity` and cast the `Context` to it.

2.  **`view.post { ... }`**:
    -   *Why we do it*: Fragment transactions are asynchronous. If we try to call `fragment.loadWithPlaceId` immediately after adding the fragment, the fragment's view might not be created yet, leading to a crash.
    -   *The Fix*: `view.post` schedules the action to run *after* the current message queue is processed, ensuring the view hierarchy is ready.

3.  **Unique View IDs (`View.generateViewId()`)**:
    -   *Why*: If you have multiple `AndroidView`s hosting fragments (even if one is hidden), they need distinct IDs so the `FragmentManager` doesn't get confused about which container holds which fragment.


---
name: places-android
description: Guide for integrating the Places SDK for Android into an application. Use when users ask to add Places, autocomplete, place details, or search for places.
---

# Places SDK for Android Integration

You are an expert Android developer specializing in modern Android architecture. Before generating any code, ask the user the following questions to tailor the solution:

### 📋 Design & Architectural Questions to Ask the User

*   **UI Framework**: Are you using Jetpack Compose or standard UI Views?
*   **Widget vs Custom UI**: Do you want to use the pre-built Google Autocomplete Widget (Dialog/Overlay) or build a completely custom programmatic search bar?
*   **Compact vs Full Details**: Do you want a compact half-sheet overlay (`PlaceDetailsCompactFragment`) or a full-page details viewer (`PlaceDetailsFragment`)?
*   **Cost & Field Scoping**: What exact fields do you need (e.g., `DISPLAY_NAME`, `FORMATTED_ADDRESS`, `PHOTO_METADATAS`)? Limiting fields saves costs!
*   **Theming Options**: Are you using Material 3 themes so we can bridge default styling automatically?

---

## 1. Setup Dependencies

Add the necessary dependencies to your module-level `build.gradle.kts` file. It is recommended to use the Versions Catalog if available:

```toml
[versions]
places = "5.1.1" # x-release-please-version

[libraries]
places = { group = "com.google.android.libraries.places", name = "places", version.ref = "places" }
```

Then in `build.gradle.kts`:

```kotlin
dependencies {
    implementation(libs.places)
}
```

## 2. Setup the Secrets Gradle Plugin

Use the Secrets Gradle Plugin for Android to inject the API key securely into your project (e.g., via `BuildConfig`), so you can access it programmatically during initialization.

Ensure you have the plugin applied in your app-level `build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.secrets.gradle.plugin)
}

secrets {
    propertiesFileName = "secrets.properties"
    defaultPropertiesFileName = "local.defaults.properties"
}
```

Add your API Key to `secrets.properties`:

```properties
PLACES_API_KEY=YOUR_API_KEY
```

## 3. Initialize the Places SDK

In your `Application` or `Activity` (before accessing any Places APIs, usually inside `onCreate`), initialize the Places SDK.

### Kotlin

```kotlin
import com.google.android.libraries.places.api.Places

class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        val apiKey = BuildConfig.PLACES_API_KEY
        if (apiKey.isNotEmpty()) {
            Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)
        }
    }
}
```

### Java

```java
import com.google.android.libraries.places.api.Places;

public class DemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        String apiKey = BuildConfig.PLACES_API_KEY;
        if (!apiKey.isEmpty()) {
            Places.initializeWithNewPlacesApiEnabled(getApplicationContext(), apiKey);
        }
    }
}
```

## 4. Best Practices & Guidelines

*   **Prefer Places UI Kit**: For displaying place details (photos, reviews, addresses), prefer using the **Places UI Kit** over manual programmatic retrieval. It provides pre-built, beautifully designed, and automatically maintained UI components!
*   **Null Safety & Validation**: Handle nulls defensively for optional parameters (e.g. Place fields).
*   **Scoped Fields**: Always specify *only* parameters that are needed (e.g. `Place.Field.ID`, `Place.Field.DISPLAY_NAME`) to avoid over-billing.
*   **Coroutine Extensions**: Use Kotlin Coroutines extensions (`places-ktx` if available) to make code cleaner.
*   **Location Permission**: Location permissions are optional but helpful. `ACCESS_COARSE_LOCATION` is sufficient for biasing prediction calls (like searching search results) to general cities. `ACCESS_FINE_LOCATION` is necessary only for exact current position tracking. Declare them in your `AndroidManifest.xml`:

    ```xml
    <manifest xmlns:android="http://schemas.android.com/apk/res/android" ...>
        <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
        <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    </manifest>
    ```

## 5. Compose Interop with Places UI Kit

The Places UI Kit (`PlaceDetailsCompactFragment` and `PlaceDetailsFragment`) are View-based. To use them in Jetpack Compose, use `AndroidView` to host a `FragmentContainerView`.

### Key Pattern: Fragment Container in Compose

*   **Access FragmentManager**: Use standard `LocalActivity.current as FragmentActivity` to access the support FragmentManager. Avoid casting `LocalContext.current` directly to Activity.
*   **Deferred Updates**: Inside the `AndroidView` `update` block, always wrap calls (like `.loadWithPlaceId()`) in `view.post { ... }` to ensure updates run *after* the layout is inflated and bindings are stable.

```kotlin
@Composable
fun PlaceDetailsCompactView(
    placeId: String,
    onDismiss: () -> Unit
) {
    val fragmentContainerId = remember { View.generateViewId() }
    val fragmentManager = (LocalActivity.current as FragmentActivity).supportFragmentManager

    val fragment = remember {
        PlaceDetailsCompactFragment.newInstance(
            PlaceDetailsCompactFragment.ALL_CONTENT,
            Orientation.VERTICAL
        )
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                FragmentContainerView(context).apply {
                    id = fragmentContainerId
                    if (fragmentManager.findFragmentById(fragmentContainerId) == null) {
                        fragmentManager.beginTransaction()
                            .add(fragmentContainerId, fragment)
                            .commit()
                    }
                }
            },
            update = { view ->
                // Ensures updates run after view hierarchy is ready
                view.post {
                    fragment.loadWithPlaceId(placeId)
                }
            }
        )
    }
}
```

## 📏 6. Advanced Compose Viewports & BottomSheetScaffold

When hosting UI Kit fragments inside navigation drawers or overlays, follow these architectural bounds to avoid viewport clipping snags:

*   **System Viewport Edge Overlap**: If using `enableEdgeToEdge()` and your container loses standard `Scaffold` body padding context, manually append `Modifier.statusBarsPadding()` to avoid overlapping with system status bar text:
    ```kotlin
    Column(modifier = modifier.statusBarsPadding()) { 
        // Beautiful search results sit safely under the status bar
    }
    ```
*   **Unified Sheet Content State**: To achieve clean mutual exclusivity between "Compact" and "Full" details click toggles, hoist the viewport state to a high enum variable level:
    ```kotlin
    enum class DetailsUiType { COMPACT, FULL }
    ```
    Track `currentUiType` at the Activity level and pass it to a single shared `BottomSheetScaffold`. Users can swipe to dismiss natively without custom button overrides!

## 7. Autocomplete with Compose (Widget)

To implement autocomplete in Compose, use `ActivityResultContracts.StartActivityForResult` with an Intent from `Autocomplete.IntentBuilder`. This is the recommended way to use the pre-built widget, as it handles session tokens and debouncing automatically.

```kotlin
@Composable
fun AutocompleteSearchButton() {
    val context = LocalContext.current
    
    val fields = listOf(Place.Field.ID, Place.Field.DISPLAY_NAME, Place.Field.FORMATTED_ADDRESS)
    val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
        .build(context)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val place = Autocomplete.getPlaceFromIntent(result.data!!)
            Log.d("Autocomplete", "Place selected: ${place.name}")
        }
    }

    Button(onClick = { launcher.launch(intent) }) {
        Text("Search Places")
    }
}
```

## 8. Execution Steps

1. Add the Places SDK dependencies to `build.gradle.kts`.
2. Set up the Secrets Gradle Plugin in `build.gradle.kts`.
3. Implement initialization (e.g., in a subclass of `Application`).
4. Provide a summary of how to use it (retrieve place details, display autocomplete).

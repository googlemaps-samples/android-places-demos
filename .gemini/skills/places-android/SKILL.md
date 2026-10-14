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
places = "6.0.2" # x-release-please-version

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

## 5. Native Jetpack Compose Components (Places SDK 6.0.2+)

Places SDK for Android 6.0.2+ includes native Jetpack Compose UI components in `com.google.android.libraries.places.compose`:
- **`AdvancedPlaceDetailsCompact`**: A native Composable for displaying compact place details with configurable content (`AdvancedPlaceDetailsCompactContent`), actions (`MainAction` / `CornerAction`), and custom composable content slots (`basicSectionTop` and `basicSectionBottom`).
- **`PlaceMarker` & `rememberPlaceMarkerState`**: A native Composable map marker designed to be rendered inside `MarkerComposable` from `maps-compose`, displaying the place type icon (`MarkerIcon.Type`) and rich attribute badges (`MarkerText.Rating`, `MarkerText.PriceRange`, `MarkerText.OpenStatus`, or `MarkerText.Custom`).

### Native `AdvancedPlaceDetailsCompact` with Custom Content Slots

```kotlin
import com.google.android.libraries.places.compose.AdvancedPlaceDetailsCompact
import com.google.android.libraries.places.compose.AdvancedPlaceDetailsCompactContent
import com.google.android.libraries.places.compose.Orientation
import com.google.android.libraries.places.compose.PlaceReference
import com.google.android.libraries.places.compose.action.MainAction

@Composable
fun PlaceDetailsCard(placeId: String, onDismiss: () -> Unit) {
    AdvancedPlaceDetailsCompact(
        reference = PlaceReference.PlaceId(placeId),
        orientation = Orientation.VERTICAL,
        content = AdvancedPlaceDetailsCompactContent.ALL_CONTENT,
        mainActions = {
            listOf(
                MainAction.OpenDirections(),
                MainAction.OpenWebsite(),
                MainAction.Call(),
                MainAction.OpenInMaps()
            )
        },
        onPlaceLoadFailed = { onDismiss() }
    ) {
        basicSectionTop {
            Text(
                text = "Featured Place",
                style = MaterialTheme.typography.labelSmall
            )
        }
        basicSectionBottom {
            val summary = this.place.editorialSummary
            if (!summary.isNullOrBlank()) {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
```

### Native `PlaceMarker` on `GoogleMap`

```kotlin
import androidx.compose.ui.geometry.Offset
import com.google.android.libraries.places.compose.PlaceMarker
import com.google.android.libraries.places.compose.PlaceReference
import com.google.android.libraries.places.compose.marker.MarkerIcon
import com.google.android.libraries.places.compose.marker.MarkerText
import com.google.android.libraries.places.compose.rememberPlaceMarkerState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState

@Composable
fun MapWithPlaceMarker(placeId: String) {
    val markerState = rememberPlaceMarkerState(
        reference = PlaceReference.PlaceId(placeId),
        icon = MarkerIcon.Type,
        text = MarkerText.Rating
    )
    GoogleMap {
        val location = markerState.location
        if (location != null) {
            MarkerComposable(
                keys = arrayOf(markerState.isLoaded, markerState.error != null, markerState.place ?: ""),
                state = remember(location) { MarkerState(position = location) },
                anchor = Offset(0.5f, 1.0f),
                title = markerState.contentDescription
            ) {
                PlaceMarker(state = markerState)
            }
        }
    }
}
```

### Hosting Full `PlaceDetailsFragment` via `AndroidView`

For the full-screen `PlaceDetailsFragment` (which is a Fragment), use `AndroidView` with a `FragmentContainerView`:

```kotlin
@Composable
fun PlaceDetailsFullView(
    placeId: String,
    onDismiss: () -> Unit
) {
    val fragmentContainerId = remember { View.generateViewId() }
    val fragmentManager = (LocalActivity.current as FragmentActivity).supportFragmentManager

    val fragment = remember {
        PlaceDetailsFragment.newInstance(
            PlaceDetailsFragment.STANDARD_CONTENT,
            com.google.android.libraries.places.widget.model.Orientation.VERTICAL
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

When hosting UI Kit components inside navigation drawers or overlays, follow these architectural bounds to avoid viewport clipping snags:

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

## 7. Autocomplete with Compose (`PlaceAutocomplete` & `BasicPlaceAutocomplete`)

In Places SDK 6.0.2+, legacy `Autocomplete`, `AutocompleteActivity`, `AutocompleteActivityMode`, and `AutocompleteSupportFragment` have been removed. Use `PlaceAutocomplete` (which returns an `AutocompletePrediction` and session token) or `BasicPlaceAutocomplete` (which returns a basic `Place` with ID, display name, and formatted address) with `ActivityResultContracts.StartActivityForResult`:

```kotlin
import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.google.android.libraries.places.widget.PlaceAutocomplete

@Composable
fun AutocompleteSearchButton() {
    val context = LocalContext.current
    val intent = PlaceAutocomplete.IntentBuilder().build(context)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                val prediction = PlaceAutocomplete.getPredictionFromIntent(data)
                Log.d("Autocomplete", "Prediction selected: ${prediction?.getPrimaryText(null)} (${prediction?.placeId})")
            }
        }
    }

    Button(onClick = { launcher.launch(intent) }) {
        Text("Search Places")
    }
}
```

## 8. Migration Guide (Places SDK 4.x / 5.x → 6.0.x+)

Places SDK `6.0.0` permanently removed the deprecated Legacy Places API classes, methods, and `Place.Field` constants. Use this reference table and code patterns when migrating existing apps to `6.0.2+`:

### Removed Classes & Methods Mapping

| Legacy API (Removed in 6.0.0) | Places SDK 6.0.x+ Replacement |
| :--- | :--- |
| `Places.initialize(context, apiKey)` | `Places.initializeWithNewPlacesApiEnabled(context, apiKey)` |
| `Autocomplete.IntentBuilder(AutocompleteActivityMode, fields)` | `PlaceAutocomplete.IntentBuilder()` (returns `AutocompletePrediction` + session token) or `BasicPlaceAutocomplete.IntentBuilder()` (returns basic `Place`) |
| `Autocomplete.getPlaceFromIntent(intent)` | `PlaceAutocomplete.getPredictionFromIntent(intent)` + `PlaceAutocomplete.getSessionTokenFromIntent(intent)` (then call `fetchPlace`), **or** `BasicPlaceAutocomplete.getPlaceFromIntent(intent)` |
| `Autocomplete.getStatusFromIntent(intent)` | `PlaceAutocomplete.getResultStatusFromIntent(intent)` or `BasicPlaceAutocomplete.getResultStatusFromIntent(intent)` |
| `AutocompleteActivity.RESULT_ERROR` | `PlaceAutocompleteActivity.RESULT_ERROR` |
| `AutocompleteSupportFragment` / `PlaceSelectionListener` | Launch `PlaceAutocomplete` or `BasicPlaceAutocomplete` via `registerForActivityResult`, or build a custom search bar with `PlacesClient.findAutocompletePredictions` |
| `FindCurrentPlaceRequest` / `placesClient.findCurrentPlace(request)` | `SearchNearbyRequest.newInstance(CircularBounds.newInstance(latLng, radiusMeters), placeFields)` + `placesClient.searchNearby(request)` |
| `FetchPhotoRequest` / `placesClient.fetchPhoto(request)` | `FetchResolvedPhotoUriRequest.builder(photoMetadata).setMaxWidth(w).setMaxHeight(h).build()` + `placesClient.fetchResolvedPhotoUri(request)` (load returned `Uri` with Coil/Glide) |

### Removed `Place.Field` Constants & `Place` Getters Mapping

| Legacy Field / Getter (Removed in 6.0.0) | Places SDK 6.0.x+ Replacement |
| :--- | :--- |
| `Place.Field.NAME` / `place.name` | `Place.Field.DISPLAY_NAME` / `place.displayName` |
| `Place.Field.ADDRESS` / `place.address` | `Place.Field.FORMATTED_ADDRESS` / `place.formattedAddress` |
| `Place.Field.LAT_LNG` / `place.latLng` | `Place.Field.LOCATION` / `place.location` |
| `Place.Field.PHONE_NUMBER` / `place.phoneNumber` | `Place.Field.INTERNATIONAL_PHONE_NUMBER` / `place.internationalPhoneNumber` (or `NATIONAL_PHONE_NUMBER`) |
| `place.types` (`Place.Field.TYPES` constant unchanged) | `place.placeTypes: List<String>` (with `Place.Field.TYPES`) or `place.primaryType` (with `Place.Field.PRIMARY_TYPE`) |
| `Place.Field.PRICE_LEVEL` (single value only) | `Place.Field.PRICE_LEVEL` (`place.priceLevel`) remains; use `Place.Field.PRICE_RANGE` (`place.priceRange`) for min/max price ranges |

### Key Migration Snippets

#### A. Autocomplete + Session Token → `FetchPlaceRequest`
Because `PlaceAutocomplete` returns an `AutocompletePrediction` (not a full `Place`), pass both the `placeId` and the session token from the result intent into `FetchPlaceRequest` to complete the billing session:

```kotlin
val prediction = PlaceAutocomplete.getPredictionFromIntent(data)
val sessionToken = PlaceAutocomplete.getSessionTokenFromIntent(data)
if (prediction != null) {
    val placeFields = listOf(
        Place.Field.ID,
        Place.Field.DISPLAY_NAME,
        Place.Field.FORMATTED_ADDRESS,
        Place.Field.ADDRESS_COMPONENTS,
        Place.Field.LOCATION
    )
    val request = FetchPlaceRequest.builder(prediction.placeId, placeFields)
        .setSessionToken(sessionToken)
        .build()
    placesClient.fetchPlace(request).addOnSuccessListener { response ->
        val place = response.place
        // Use place.displayName, place.formattedAddress, place.location
    }
}
```

#### B. `FindCurrentPlaceRequest` → `SearchNearbyRequest`
Obtain the device's current coordinates via `FusedLocationProviderClient`, wrap them in `CircularBounds`, and call `placesClient.searchNearby`:

```kotlin
val bounds = CircularBounds.newInstance(currentLatLng, /* radiusMeters = */ 1000.0)
val placeFields = listOf(Place.Field.ID, Place.Field.DISPLAY_NAME, Place.Field.LOCATION)
val request = SearchNearbyRequest.newInstance(bounds, placeFields)
placesClient.searchNearby(request).addOnSuccessListener { response ->
    for (place in response.places) {
        Log.d("Places", "${place.displayName}: ${place.location}")
    }
}
```

#### C. `FetchPhotoRequest` → `FetchResolvedPhotoUriRequest`
Instead of downloading raw `Bitmap` bytes via `fetchPhoto`, resolve a cacheable `Uri` and load it with Coil or Glide:

```kotlin
val photoMetadata = place.photoMetadatas?.firstOrNull() ?: return
val photoRequest = FetchResolvedPhotoUriRequest.builder(photoMetadata)
    .setMaxWidth(800)
    .setMaxHeight(600)
    .build()
placesClient.fetchResolvedPhotoUri(photoRequest).addOnSuccessListener { response ->
    val uri = response.uri
    // Load uri into ImageView / AsyncImage via Glide or Coil
}
```

## 9. Execution Steps

1. Add the Places SDK dependencies to `build.gradle.kts`.
2. Set up the Secrets Gradle Plugin in `build.gradle.kts`.
3. Implement initialization (e.g., in a subclass of `Application`).
4. Provide a summary of how to use it (retrieve place details, display autocomplete).

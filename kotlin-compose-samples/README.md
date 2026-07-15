# Kotlin Compose Samples (`:kotlin-compose-samples`)

This module provides comprehensive sample demonstrations written in **Kotlin** using modern **Jetpack Compose UI**, **Material 3**, **Navigation Compose**, and **Coil** to showcase the full feature set and integration patterns of the Google Places SDK for Android (v5.3.0).

---

## 🗺️ Feature Catalog & Status

| Feature / Demo Area | Status | Source Code | Preview | Demonstrated APIs & Capabilities |
| :--- | :--- | :--- | :--- | :--- |
| **Core Initialization & App Check** | ![Supported](https://img.shields.io/badge/Status-Supported-brightgreen) | [`CoreInitializationDemoScreen.kt`](src/main/java/com/google/android/libraries/places/samples/kotlincompose/screens/CoreInitializationDemoScreen.kt) | <img src="screenshots/core-initialization-demo.png" width="121" alt="Core Initialization"/> | `Places.initializeWithNewPlacesApiEnabled()`, `Places.setPlacesAppCheckTokenProvider()`, `Places.deinitialize()`, `Places.isInitialized()`. Declarative initialization toggles in Compose. |
| **Search & Discovery** | ![Supported](https://img.shields.io/badge/Status-Supported-brightgreen) | [`SearchAndDiscoveryDemoScreen.kt`](src/main/java/com/google/android/libraries/places/samples/kotlincompose/screens/SearchAndDiscoveryDemoScreen.kt) | <img src="screenshots/search-and-discovery-demo.png" width="121" alt="Search & Discovery"/> | `searchByText()`, `searchNearby()`, `findCurrentPlace()`, `findAutocompletePredictions()`. Material 3 `SearchBar`, `FilterChip`, location permissions launcher, and lazy list results. |
| **Place Details & Photos** | ![Supported](https://img.shields.io/badge/Status-Supported-brightgreen) | [`PlaceDetailsAndPhotosDemoScreen.kt`](src/main/java/com/google/android/libraries/places/samples/kotlincompose/screens/PlaceDetailsAndPhotosDemoScreen.kt) | <img src="screenshots/place-details-and-photos-demo.png" width="121" alt="Place Details & Photos"/> | `fetchPlace()`, `fetchPhoto()`, `fetchResolvedPhotoUri()`. `AddressDescriptor` cards (`Area` & `Landmark`) and Coil `AsyncImage` async rendering. |
| **Place Attributes & Hours** | ![Supported](https://img.shields.io/badge/Status-Supported-brightgreen) | [`PlaceAttributesAndHoursDemoScreen.kt`](src/main/java/com/google/android/libraries/places/samples/kotlincompose/screens/PlaceAttributesAndHoursDemoScreen.kt) | <img src="screenshots/place-attributes-and-hours-demo.png" width="121" alt="Place Attributes & Hours"/> | `isOpen()`, `ContainingPlace`, `PriceRange`, `OpeningHours`, `AccessibilityOptions`, `EVChargeOptions`, `FuelPrice`. Declarative UI state badges. |
| **Places UI Kit & Actions** | ![Supported](https://img.shields.io/badge/Status-Supported-brightgreen) | [`PlacesUIKitAndActionsDemoScreen.kt`](src/main/java/com/google/android/libraries/places/samples/kotlincompose/screens/PlacesUIKitAndActionsDemoScreen.kt) | <img src="screenshots/places-ui-kit-and-actions-demo.png" width="121" alt="Places UI Kit & Actions"/> | `AdvancedPlaceDetailsCompactFragment` interop via `AndroidViewBinding` / `AndroidView`, custom `PlaceActionProvider` (`CALL`, `OPEN_WEBSITE`, `OPEN_DIRECTIONS`, `OPEN_IN_MAPS`). |

---

## 🛠️ Key Jetpack Compose Code Implementation Examples

### 1. Declarative Search Bar with KTX Suspend Calls
```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPlacesComposable(placesClient: PlacesClient) {
    var query by remember { mutableStateOf("") }
    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }

    LaunchedEffect(query) {
        if (query.length >= 2) {
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .build()
            val response = placesClient.awaitFindAutocompletePredictions(request)
            predictions = response.autocompletePredictions
        }
    }

    SearchBar(
        query = query,
        onQueryChange = { query = it },
        onSearch = {},
        active = true,
        onActiveChange = {}
    ) {
        LazyColumn {
            items(predictions) { prediction ->
                Text(
                    text = prediction.getPrimaryText(null).toString(),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
```

### 2. Rendering Async Place Photos using Coil `AsyncImage`
```kotlin
@Composable
fun PlacePhotoComposable(placesClient: PlacesClient, photoMetadata: PhotoMetadata) {
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(photoMetadata) {
        val request = FetchResolvedPhotoUriRequest.builder(photoMetadata)
            .setMaxWidth(800)
            .setMaxHeight(600)
            .build()
        val response = placesClient.awaitFetchResolvedPhotoUri(request)
        photoUri = response.uri
    }

    photoUri?.let { uri ->
        AsyncImage(
            model = uri,
            contentDescription = "Place Photo",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentScale = ContentScale.Crop
        )
    }
}
```

### 3. Embed Places UI Kit Fragment in Compose via `AndroidView`
```kotlin
@Composable
fun AdvancedPlaceDetailsUIKitComposable(placeId: String) {
    AndroidView(
        factory = { context ->
            FragmentContainerView(context).apply {
                id = View.generateViewId()
                val fragment = AdvancedPlaceDetailsCompactFragment.newInstance(
                    AdvancedPlaceDetailsCompactFragment.ALL_CONTENT,
                    Orientation.VERTICAL,
                    R.style.CustomizedPlaceDetailsTheme
                ).apply {
                    setPlaceActionProvider(object : PlaceActionProvider {
                        override fun getMainPlaceActions(place: Place): List<PlaceAction> = listOf(
                            PlaceAction.CALL, PlaceAction.OPEN_WEBSITE, PlaceAction.OPEN_DIRECTIONS, PlaceAction.OPEN_IN_MAPS
                        )
                        override fun getCornerPlaceActions(place: Place): List<CornerPlaceAction> = emptyList()
                        override fun addPlaceActionsChangedListener(listener: PlaceActionProvider.OnChangedListener) {}
                        override fun removePlaceActionsChangedListener(listener: PlaceActionProvider.OnChangedListener) {}
                    })
                }
                (context as FragmentActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(id, fragment)
                    .commit()
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
```

---

## 🚀 Building & Running

To launch this Jetpack Compose sample module directly onto your connected device or emulator:

```bash
./gradlew :kotlin-compose-samples:installAndLaunch
```

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

/**
 * Central, single-source-of-truth registry of all Google Places SDK for Android samples
 * across `:demo-kotlin`, `:demo-java`, `:PlaceDetailsUIKit`, `:PlaceDetailsCompose`,
 * `:PlacesUIKit3D`, `:kotlin-demos`, and `:snippets`.
 *
 * Provides rich metadata, hashtags, complexity tiers, key API calls, expectations,
 * and cross-framework activity resolution for the Unified Sample Catalog and Reviewer Mode.
 */
object SampleCatalogRegistry {

    val SAMPLES: List<SampleItem> = listOf(
        // ==========================================
        // 1. Autocomplete
        // ==========================================
        SampleItem(
            id = "com.example.placesdemo.PlaceAutocompleteActivity",
            title = "Place Autocomplete",
            description = "Full-featured PlaceAutocomplete & BasicPlaceAutocomplete intents plus programmatic predictions.",
            category = "Place Autocomplete",
            complexity = Complexity.SIMPLE,
            tags = listOf("#autocomplete", "#predictions", "#sessiontoken", "#locationbias", "#places"),
            apiCalls = listOf(
                "PlaceAutocomplete.IntentBuilder().build(context)",
                "PlaceAutocomplete.getPredictionFromIntent(intent)",
                "BasicPlaceAutocomplete.IntentBuilder().build(context)",
                "BasicPlaceAutocomplete.getPlaceFromIntent(intent)",
                "FindAutocompletePredictionsRequest.builder()",
                "PlacesClient.findAutocompletePredictions(request)",
            ),
            purpose = "Demonstrates launching the full-screen/overlay PlaceAutocomplete and BasicPlaceAutocomplete widgets via IntentBuilder, as well as fetching programmatic autocomplete predictions with location bias, restriction, country, and type filters.",
            successCriteria = "• Tapping 'Place Autocomplete Activity' or 'Basic Autocomplete Activity' opens the Places Autocomplete UI.\n• Selecting a prediction returns the prediction or Place details and displays them in the response panel.\n• Tapping 'Fetch Autocomplete Predictions' executes a programmatic prediction query.",
            failureIndicators = "• Autocomplete intent immediately closes with RESULT_ERROR or DEVELOPER_ERROR.\n• Programmatic prediction request fails or returns empty results for valid queries.",
            kotlinActivity = "com.example.placesdemo.PlaceAutocompleteActivity",
            javaActivity = "com.example.placesdemo.PlaceAutocompleteActivity",
            composeActivity = "com.google.places.android.ktx.demo.AutocompleteDemoActivity",
        ),
        SampleItem(
            id = "com.example.placesdemo.AutocompleteAddressActivity",
            title = "Autocomplete Address Form",
            description = "Auto-filling a structured postal address form using Place Autocomplete and verifying location proximity on a Google Map.",
            category = "Place Autocomplete",
            complexity = Complexity.ADVANCED,
            tags = listOf("#autocomplete", "#address", "#checkout", "#map", "#location"),
            apiCalls = listOf(
                "PlaceAutocomplete.IntentBuilder().setTypesFilter(listOf(PlaceTypes.ADDRESS))",
                "PlaceAutocomplete.getPredictionFromIntent(intent)",
                "PlaceAutocomplete.getSessionTokenFromIntent(intent)",
                "FetchPlaceRequest.builder(placeId, fields).setSessionToken(sessionToken)",
                "PlacesClient.fetchPlace(request)",
                "Place.addressComponents",
                "SphericalUtil.computeDistanceBetween(currentLocation, enteredLocation)",
            ),
            purpose = "Demonstrates a real-world address checkout workflow: launching PlaceAutocomplete filtered to street addresses, fetching ADDRESS_COMPONENTS + LOCATION with the autocomplete session token, auto-populating structured address fields, and displaying a confirmation map.",
            successCriteria = "• Tapping the 'Street address' field launches PlaceAutocomplete filtered to US addresses.\n• Selecting an address populates Street, City, State, Postal Code, and Country fields, focuses the Apt/Unit field, and renders a confirmation map marker.",
            failureIndicators = "• Address fields remain blank after selecting an address prediction.\n• Confirmation map fails to inflate or center on the selected address.",
            kotlinActivity = "com.example.placesdemo.AutocompleteAddressActivity",
            javaActivity = "com.example.placesdemo.AutocompleteAddressActivity",
            composeActivity = null,
        ),
        SampleItem(
            id = "com.example.placesdemo.programmatic_autocomplete.ProgrammaticAutocompleteGeocodingActivity",
            title = "Programmatic Autocomplete & Search",
            description = "Custom as-you-type search bar querying programmatic autocomplete predictions and resolving Place coordinates.",
            category = "Place Autocomplete",
            complexity = Complexity.ADVANCED,
            tags = listOf("#autocomplete", "#searchbar", "#geocoding", "#predictions", "#recyclerview"),
            apiCalls = listOf(
                "AutocompleteSessionToken.newInstance()",
                "FindAutocompletePredictionsRequest.builder().setSessionToken(token).setQuery(query)",
                "PlacesClient.findAutocompletePredictions(request)",
                "FetchPlaceRequest.newInstance(placeId, placeFields)",
                "PlacesClient.fetchPlace(request)",
            ),
            purpose = "Demonstrates building a custom debounced as-you-type search UI backed by PlacesClient.findAutocompletePredictions with session tokens, displaying predictions in a list, and resolving the selected place.",
            successCriteria = "• Typing into the search bar triggers debounced autocomplete prediction queries with a loading indicator.\n• Tapping a prediction resolves and displays the place's coordinates or details.",
            failureIndicators = "• Typing does not trigger prediction updates.\n• Tapping a prediction crashes or fails to display the result dialog.",
            kotlinActivity = "com.example.placesdemo.programmatic_autocomplete.ProgrammaticAutocompleteGeocodingActivity",
            javaActivity = "com.example.placesdemo.programmatic_autocomplete.ProgrammaticAutocompleteToolbarActivity",
            composeActivity = "com.google.places.android.ktx.demo.PlacesSearchDemoActivity",
        ),

        // ==========================================
        // 2. Place Details & Photos
        // ==========================================
        SampleItem(
            id = "com.example.placesdemo.PlaceDetailsAndPhotosActivity",
            title = "Place Details, Photos & Icons",
            description = "Fetching Place Details by Place ID, resolving photo URIs via FetchResolvedPhotoUriRequest, and rendering Place icons.",
            category = "Place Details & Photos",
            complexity = Complexity.SIMPLE,
            tags = listOf("#placedetails", "#photos", "#icons", "#fetchplace", "#uri"),
            apiCalls = listOf(
                "FetchPlaceRequest.newInstance(placeId, placeFields)",
                "PlacesClient.fetchPlace(request)",
                "FetchResolvedPhotoUriRequest.builder(photoMetadata).setMaxWidth(w).setMaxHeight(h).build()",
                "PlacesClient.fetchResolvedPhotoUri(photoRequest)",
                "Place.iconMaskUrl",
                "Place.iconBackgroundColor",
            ),
            purpose = "Demonstrates fetching Place Details for a Place ID with customizable Place.Field selections, resolving high-resolution place photos via FetchResolvedPhotoUriRequest, and loading themed Place icon masks.",
            successCriteria = "• Tapping 'Fetch Place and Photo' retrieves details for the Place ID and prints formatted fields.\n• Checking 'Fetch Photo' resolves the photo URI and renders the image via Glide/Coil.\n• Checking 'Fetch Icon' renders the Place icon mask over its background color.",
            failureIndicators = "• FetchPlace fails with API error.\n• Photo or icon image view remains blank when the respective checkbox is enabled.",
            kotlinActivity = "com.example.placesdemo.PlaceDetailsAndPhotosActivity",
            javaActivity = "com.example.placesdemo.PlaceDetailsAndPhotosActivity",
            composeActivity = "com.google.places.android.ktx.demo.PlacesPhotoDemoActivity",
        ),
        SampleItem(
            id = "com.example.placesdemo.PlaceIsOpenActivity",
            title = "Place Open Status (isOpen)",
            description = "Checking whether a place is open at a specific date, time, and timezone using IsOpenRequest.",
            category = "Place Details & Photos",
            complexity = Complexity.SIMPLE,
            tags = listOf("#isopen", "#openinghours", "#timezone", "#placedetails"),
            apiCalls = listOf(
                "IsOpenRequest.newInstance(placeId, utcTimeMillis)",
                "IsOpenRequest.newInstance(place, utcTimeMillis)",
                "PlacesClient.isOpen(request)",
                "IsOpenResponse.isOpen",
            ),
            purpose = "Demonstrates checking whether a business is open at a chosen date, time, and timezone either directly by Place ID or using a pre-fetched Place object containing opening hours.",
            successCriteria = "• Selecting a date, time, and timezone and tapping 'Is Open (by Place ID)' or 'Fetch Place & Check Is Open' returns true/false/null open status.\n• Extra opening hours details are displayed when fetching the Place object.",
            failureIndicators = "• Date/time pickers fail to update the query timestamp.\n• isOpen request throws an unhandled exception.",
            kotlinActivity = "com.example.placesdemo.PlaceIsOpenActivity",
            javaActivity = "com.example.placesdemo.PlaceIsOpenActivity",
            composeActivity = null,
        ),
        SampleItem(
            id = "com.example.placesdemo.PlaceActionsAndAdvancedDetailsActivity",
            title = "Advanced Place Details & Place Actions",
            description = "Inspecting AddressDescriptor (areas & landmarks), ContainingPlaces, PriceRange, SearchMediaOptions, SearchReviewsOptions, and PlaceActionProvider.",
            category = "Place Details & Photos",
            complexity = Complexity.ADVANCED,
            tags = listOf("#addressdescriptor", "#landmarks", "#pricerange", "#placeactions", "#media", "#reviews"),
            apiCalls = listOf(
                "Place.Field.ADDRESS_DESCRIPTOR",
                "Place.Field.CONTAINING_PLACES",
                "Place.Field.PRICE_RANGE",
                "SearchMediaOptions.builder().setQuery(query).setRankPreference(rank).build()",
                "SearchReviewsOptions.builder().setQuery(query).setRankPreference(rank).build()",
                "PlaceActionProvider.getMainPlaceActions(place)",
            ),
            purpose = "Demonstrates advanced Places SDK data models including spatial AddressDescriptor landmarks/areas, ContainingPlaces hierarchy, PriceRange, SearchMediaOptions, SearchReviewsOptions, and custom PlaceActionProvider configuration.",
            successCriteria = "• Tapping 'Fetch Advanced Place Details' retrieves and formats AddressDescriptor landmarks/areas, ContainingPlaces, PriceRange, and configured PlaceActions for the input Place ID.",
            failureIndicators = "• Fetching advanced fields fails or crashes when formatting null/optional properties.",
            kotlinActivity = "com.example.placesdemo.PlaceActionsAndAdvancedDetailsActivity",
            javaActivity = "com.example.placesdemo.PlaceActionsAndAdvancedDetailsActivity",
            composeActivity = null,
        ),

        // ==========================================
        // 3. Places UI Kit & 3D Maps
        // ==========================================
        SampleItem(
            id = "com.example.placedetailsuikit.MainActivity",
            title = "Place Details UI Kit (Compact & Map Markers)",
            description = "Interactive map with PlaceDetailsCompactFragment (Views) or AdvancedPlaceDetailsCompact + PlaceMarker (Compose).",
            category = "Places UI Kit & 3D",
            complexity = Complexity.SIMPLE,
            tags = listOf("#uikit", "#compact", "#placemarker", "#compose", "#map"),
            apiCalls = listOf(
                "PlaceDetailsCompactFragment.newInstance(ALL_CONTENT, orientation, theme)",
                "PlaceDetailsCompactFragment.loadWithPlaceId(placeId)",
                "AdvancedPlaceDetailsCompact(placeId, orientation, content, placeLoadListener)",
                "PlaceMarker(location, id, title, icon)",
                "rememberPlaceMarkerState(position)",
            ),
            purpose = "Demonstrates integrating the Places UI Kit compact place card with a Google Map. In Kotlin Views, tapping a map POI loads PlaceDetailsCompactFragment. In Jetpack Compose, Places 6.0.2 PlaceMarker pins and AdvancedPlaceDetailsCompact with content slots are rendered.",
            successCriteria = "• Map loads and centers on user location or default city.\n• Tapping a POI or PlaceMarker opens the Place Details UI Kit compact card with photos, ratings, and details.",
            failureIndicators = "• Map fails to render or tapping a POI/marker does not load the Place Details UI Kit card.",
            kotlinActivity = "com.example.placedetailsuikit.MainActivity",
            javaActivity = null,
            composeActivity = "com.example.placedetailscompose.MainActivity",
        ),
        SampleItem(
            id = "com.example.placedetailsuikit.compact.ConfigurablePlaceDetailsActivity",
            title = "Configurable Compact Place Details UI Kit",
            description = "Dynamically toggling visible content sections in PlaceDetailsCompactFragment at runtime.",
            category = "Places UI Kit & 3D",
            complexity = Complexity.ADVANCED,
            tags = listOf("#uikit", "#compact", "#configurable", "#viewmodel", "#flow"),
            apiCalls = listOf(
                "PlaceDetailsCompactFragment.newInstance(selectedContent, orientation, theme)",
                "PlaceDetailsCompactFragment.Content.entries",
                "PlaceDetailsCompactFragment.loadWithPlaceId(placeId)",
                "AdvancedPlaceDetailsCompact(placeId, content = setOf(Content.MEDIA, Content.RATING, ...))",
            ),
            purpose = "Demonstrates runtime customization of the content sections displayed inside PlaceDetailsCompactFragment (Views) or AdvancedPlaceDetailsCompact (Compose).",
            successCriteria = "• Tapping 'Configure' opens the content section selector.\n• Toggling content sections immediately reloads the compact Place Details widget to reflect the selected fields.",
            failureIndicators = "• Toggling content items does not update the displayed Place Details card.",
            kotlinActivity = "com.example.placedetailsuikit.compact.ConfigurablePlaceDetailsActivity",
            javaActivity = null,
            composeActivity = "com.example.placedetailscompose.MainActivity",
        ),
        SampleItem(
            id = "com.example.placedetailsuikit.full.FullConfigurablePlaceDetailsActivity",
            title = "Configurable Full Place Details UI Kit",
            description = "Full-screen PlaceDetailsFragment with runtime content section configuration.",
            category = "Places UI Kit & 3D",
            complexity = Complexity.ADVANCED,
            tags = listOf("#uikit", "#fullscreen", "#configurable", "#placedetails"),
            apiCalls = listOf(
                "PlaceDetailsFragment.newInstance(selectedContent, orientation, theme)",
                "PlaceDetailsFragment.Content.entries",
                "PlaceDetailsFragment.loadWithPlaceId(placeId)",
            ),
            purpose = "Demonstrates the full-screen PlaceDetailsFragment from the Places UI Kit with dynamic content selection.",
            successCriteria = "• Tapping any POI on the map opens the full PlaceDetailsFragment.\n• Tapping 'Configure' allows enabling/disabling full-card content sections on the fly.",
            failureIndicators = "• Full PlaceDetailsFragment fails to load or dismiss button does not close the card.",
            kotlinActivity = "com.example.placedetailsuikit.full.FullConfigurablePlaceDetailsActivity",
            javaActivity = null,
            composeActivity = null,
        ),
        SampleItem(
            id = "com.example.placedetailsuikit.advanced.AdvancedConfigurablePlaceDetailsActivity",
            title = "Advanced Place Details UI Kit (Actions & Media)",
            description = "Places UI Kit AdvancedPlaceDetailsCompactFragment with custom PlaceActionProvider buttons and SearchMediaOptions.",
            category = "Places UI Kit & 3D",
            complexity = Complexity.ADVANCED,
            tags = listOf("#uikit", "#advanced", "#placeactions", "#media", "#compose"),
            apiCalls = listOf(
                "AdvancedPlaceDetailsCompactFragment.newInstance(ALL_CONTENT, orientation, theme)",
                "AdvancedPlaceDetailsCompactFragment.setPlaceActionProvider(provider)",
                "AdvancedPlaceDetailsCompactFragment.applySearchMediaOptions(options)",
                "AdvancedPlaceDetailsCompact(placeId, searchMediaOptions, searchReviewsOptions, mainActions)",
            ),
            purpose = "Demonstrates the AdvancedPlaceDetailsCompactFragment (Views) and AdvancedPlaceDetailsCompact composable (Compose) with custom PlaceAction buttons (Call, Website, Directions, Open in Maps) and filtered media/review options.",
            successCriteria = "• Tapping a POI on the map loads the Advanced Place Details card with custom action buttons and media options.",
            failureIndicators = "• Advanced Place Details card fails to load or action buttons are missing.",
            kotlinActivity = "com.example.placedetailsuikit.advanced.AdvancedConfigurablePlaceDetailsActivity",
            javaActivity = null,
            composeActivity = "com.example.placedetailscompose.MainActivity",
        ),
        SampleItem(
            id = "com.example.placesuikit3d.MainActivity",
            title = "Places UI Kit on 3D Maps",
            description = "Immersive 3D Map (Map3DView) integrated with PlaceDetailsCompactFragment and camera fly-to animations.",
            category = "Places UI Kit & 3D",
            complexity = Complexity.ADVANCED,
            tags = listOf("#maps3d", "#uikit", "#3d", "#camera", "#compose"),
            apiCalls = listOf(
                "Map3DView(modifier, onMap3DViewReady)",
                "GoogleMap3D.setMap3DClickListener(listener)",
                "GoogleMap3D.flyCameraTo(FlyToOptions)",
                "PlaceDetailsCompactFragment.newInstance(ALL_CONTENT, orientation)",
                "PlaceDetailsCompactFragment.loadWithPlaceId(placeId)",
            ),
            purpose = "Demonstrates combining the Google Maps 3D SDK (Map3DView and GoogleMap3D camera fly-to animations) with the Places UI Kit PlaceDetailsCompactFragment in a Jetpack Compose application.",
            successCriteria = "• 3D terrain and buildings render around the initial landmark.\n• Tapping a 3D place marker/POI flies the 3D camera to the location and opens the PlaceDetailsCompactFragment card.",
            failureIndicators = "• 3D map remains blank or tapping a POI does not open the Place Details card.",
            kotlinActivity = null,
            javaActivity = null,
            composeActivity = "com.example.placesuikit3d.MainActivity",
        ),

        // ==========================================
        // 4. Nearby Search / Current Place
        // ==========================================
        SampleItem(
            id = "com.example.placesdemo.TextAndNearbySearchActivity",
            title = "Text Search & Nearby Search",
            description = "Querying places with SearchByTextRequest and SearchNearbyRequest using CircularBounds, type filtering, and openNow constraints.",
            category = "Nearby Search",
            complexity = Complexity.ADVANCED,
            tags = listOf("#textsearch", "#nearby", "#searchbytext", "#searchnearby", "#circularbounds", "#compose", "#ktx"),
            apiCalls = listOf(
                "SearchByTextRequest.builder(query, placeFields).setLocationBias(bounds).setIncludedType(type).setOpenNow(openNow).build()",
                "PlacesClient.searchByText(request)",
                "SearchNearbyRequest.builder(bounds, placeFields).setIncludedTypes(listOf(type)).build()",
                "PlacesClient.searchNearby(request)",
                "PlacesClient.awaitSearchByText(textQuery, placeFields) { ... }",
                "PlacesClient.awaitSearchNearby(locationRestriction, placeFields) { ... }",
            ),
            purpose = "Demonstrates side-by-side Text Search (SearchByTextRequest / awaitSearchByText) and Nearby Search (SearchNearbyRequest / awaitSearchNearby) with CircularBounds, place type filtering, and openNow constraints across Kotlin Views, Java Views, and Jetpack Compose + KTX.",
            successCriteria = "• Tapping 'Search By Text' / 'awaitSearchByText' executes a text search with the configured query, CircularBounds location bias, includedType, and openNow filter.\n• Tapping 'Search Nearby' / 'awaitSearchNearby' executes a nearby search within the CircularBounds restriction.",
            failureIndicators = "• Blank query or non-positive radius fails to show a validation message.\n• Valid search requests throw an unhandled exception.",
            kotlinActivity = "com.example.placesdemo.TextAndNearbySearchActivity",
            javaActivity = "com.example.placesdemo.TextAndNearbySearchActivity",
            composeActivity = "com.google.places.android.ktx.demo.PlacesTextAndNearbySearchDemoActivity",
        ),
        SampleItem(
            id = "com.example.placesdemo.CurrentPlaceActivity",
            title = "Nearby Search (SearchNearbyRequest)",
            description = "Searching for nearby places within a circular geographic bound using PlacesClient.searchNearby.",
            category = "Nearby Search",
            complexity = Complexity.SIMPLE,
            tags = listOf("#nearby", "#searchnearby", "#circularbounds", "#location"),
            apiCalls = listOf(
                "CircularBounds.newInstance(centerLatLng, radiusMeters)",
                "SearchNearbyRequest.newInstance(locationRestriction, placeFields)",
                "PlacesClient.searchNearby(request)",
            ),
            purpose = "Demonstrates querying nearby places within a CircularBounds region using SearchNearbyRequest and customizable Place.Field selections.",
            successCriteria = "• Tapping 'Find Current / Nearby Place' requests location permission (if needed) and executes PlacesClient.searchNearby, listing returned places in the response view.",
            failureIndicators = "• Permission prompt fails or searchNearby returns an unhandled error.",
            kotlinActivity = "com.example.placesdemo.CurrentPlaceActivity",
            javaActivity = "com.example.placesdemo.CurrentPlaceActivity",
            composeActivity = null,
        ),

        // ==========================================
        // 5. DevSite Documentation Snippets (:snippets)
        // ==========================================
        SampleItem(
            id = "com.google.places.kotlin.GetStartedActivity",
            title = "Snippet: Get Started & Initialize SDK",
            description = "DevSite documentation snippet showing Places.initializeWithNewPlacesApiEnabled and Places.createClient.",
            category = "Documentation Snippets",
            complexity = Complexity.SNIPPET,
            tags = listOf("#snippet", "#init", "#getstarted", "#placesclient"),
            apiCalls = listOf(
                "Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)",
                "Places.createClient(context)",
            ),
            purpose = "Canonical DevSite documentation snippet demonstrating how to initialize the Places SDK with the New Places API enabled and create a PlacesClient instance.",
            successCriteria = "• Activity launches cleanly, initializes Places SDK with the configured API key, and creates a PlacesClient without error.",
            failureIndicators = "• Activity crashes or shows an unconfigured API key toast.",
            kotlinActivity = "com.google.places.kotlin.GetStartedActivity",
            javaActivity = "com.google.places.GetStartedActivity",
            composeActivity = null,
        ),
        SampleItem(
            id = "com.google.places.kotlin.PlaceAutocompleteActivity",
            title = "Snippet: Place Autocomplete",
            description = "DevSite documentation snippet for PlaceAutocomplete and programmatic prediction requests.",
            category = "Documentation Snippets",
            complexity = Complexity.SNIPPET,
            tags = listOf("#snippet", "#autocomplete", "#predictions"),
            apiCalls = listOf(
                "PlaceAutocomplete.IntentBuilder().build(context)",
                "PlaceAutocomplete.getPredictionFromIntent(intent)",
                "FindAutocompletePredictionsRequest.builder()",
                "PlacesClient.findAutocompletePredictions(request)",
            ),
            purpose = "Canonical DevSite code snippet illustrating PlaceAutocomplete intent launching and programmatic prediction queries in Kotlin and Java.",
            successCriteria = "• Activity launches and executes the Place Autocomplete snippet flow without error.",
            failureIndicators = "• Crash or missing resource on launch.",
            kotlinActivity = "com.google.places.kotlin.PlaceAutocompleteActivity",
            javaActivity = "com.google.places.PlaceAutocompleteActivity",
            composeActivity = null,
        ),
        SampleItem(
            id = "com.google.places.kotlin.PlaceDetailsActivity",
            title = "Snippet: Place Details",
            description = "DevSite documentation snippet for fetching Place Details with FetchPlaceRequest.",
            category = "Documentation Snippets",
            complexity = Complexity.SNIPPET,
            tags = listOf("#snippet", "#placedetails", "#fetchplace"),
            apiCalls = listOf(
                "FetchPlaceRequest.newInstance(placeId, placeFields)",
                "PlacesClient.fetchPlace(request)",
            ),
            purpose = "Canonical DevSite code snippet demonstrating how to request Place Details by Place ID.",
            successCriteria = "• Activity launches and executes the FetchPlaceRequest snippet flow.",
            failureIndicators = "• Crash on launch.",
            kotlinActivity = "com.google.places.kotlin.PlaceDetailsActivity",
            javaActivity = "com.google.places.PlaceDetailsActivity",
            composeActivity = null,
        ),
        SampleItem(
            id = "com.google.places.kotlin.PlacePhotosActivity",
            title = "Snippet: Place Photos",
            description = "DevSite documentation snippet for fetching Place Photos and resolving photo URIs.",
            category = "Documentation Snippets",
            complexity = Complexity.SNIPPET,
            tags = listOf("#snippet", "#photos", "#fetchresolvedphotouri"),
            apiCalls = listOf(
                "Place.Field.PHOTO_METADATAS",
                "FetchResolvedPhotoUriRequest.builder(photoMetadata).setMaxWidth(500).setMaxHeight(300).build()",
                "PlacesClient.fetchResolvedPhotoUri(photoRequest)",
            ),
            purpose = "Canonical DevSite code snippet demonstrating how to retrieve PhotoMetadata from a Place and resolve a photo URI via FetchResolvedPhotoUriRequest.",
            successCriteria = "• Activity launches cleanly and executes the Place Photos snippet.",
            failureIndicators = "• Crash on launch.",
            kotlinActivity = "com.google.places.kotlin.PlacePhotosActivity",
            javaActivity = "com.google.places.PlacePhotosActivity",
            composeActivity = null,
        ),
        SampleItem(
            id = "com.google.places.kotlin.CurrentPlaceActivity",
            title = "Snippet: Nearby Search (SearchNearby)",
            description = "DevSite documentation snippet for searching nearby places within CircularBounds.",
            category = "Documentation Snippets",
            complexity = Complexity.SNIPPET,
            tags = listOf("#snippet", "#nearby", "#searchnearby"),
            apiCalls = listOf(
                "CircularBounds.newInstance(center, radius)",
                "SearchNearbyRequest.builder(locationRestriction, placeFields).build()",
                "PlacesClient.searchNearby(request)",
            ),
            purpose = "Canonical DevSite code snippet demonstrating how to search for nearby places around a coordinate using SearchNearbyRequest.",
            successCriteria = "• Activity launches cleanly and demonstrates the SearchNearbyRequest snippet.",
            failureIndicators = "• Crash on launch.",
            kotlinActivity = "com.google.places.kotlin.CurrentPlaceActivity",
            javaActivity = "com.google.places.CurrentPlaceActivity",
            composeActivity = null,
        ),
        SampleItem(
            id = "com.google.places.kotlin.PlaceIsOpenActivity",
            title = "Snippet: Place Is Open",
            description = "DevSite documentation snippet for checking if a place is open via IsOpenRequest.",
            category = "Documentation Snippets",
            complexity = Complexity.SNIPPET,
            tags = listOf("#snippet", "#isopen", "#openinghours"),
            apiCalls = listOf(
                "IsOpenRequest.newInstance(place, utcTimeMillis)",
                "IsOpenRequest.newInstance(placeId, utcTimeMillis)",
                "PlacesClient.isOpen(request)",
            ),
            purpose = "Canonical DevSite code snippet demonstrating how to check whether a place is currently open or open at a specific timestamp.",
            successCriteria = "• Activity launches cleanly and demonstrates the IsOpenRequest snippet.",
            failureIndicators = "• Crash on launch.",
            kotlinActivity = "com.google.places.kotlin.PlaceIsOpenActivity",
            javaActivity = "com.google.places.PlaceIsOpenActivity",
            composeActivity = null,
        ),
        SampleItem(
            id = "com.google.places.kotlin.PlacesIconActivity",
            title = "Snippet: Place Icon & Background Color",
            description = "DevSite documentation snippet for rendering a Place's thematic icon mask and background color.",
            category = "Documentation Snippets",
            complexity = Complexity.SNIPPET,
            tags = listOf("#snippet", "#icons", "#placedetails"),
            apiCalls = listOf(
                "Place.Field.ICON_MASK_URL",
                "Place.Field.ICON_BACKGROUND_COLOR",
                "Place.iconMaskUrl",
                "Place.iconBackgroundColor",
            ),
            purpose = "Canonical DevSite code snippet demonstrating how to fetch and render a Place's icon mask URL and background color.",
            successCriteria = "• Activity launches cleanly and demonstrates the Place icon snippet.",
            failureIndicators = "• Crash on launch.",
            kotlinActivity = "com.google.places.kotlin.PlacesIconActivity",
            javaActivity = "com.google.places.PlacesIconActivity",
            composeActivity = null,
        ),
    )

    /**
     * Returns all unique hashtags across all registered samples, sorted alphabetically.
     */
    fun getAllTags(): List<String> = SAMPLES.flatMap { it.tags }.distinct().sorted()

    /**
     * Returns all unique categories across all registered samples, sorted alphabetically.
     */
    fun getCategories(): List<String> = SAMPLES.map { it.category }.distinct().sorted()

    /**
     * Filters samples by [framework], [complexity], [selectedTags], and free-text [searchQuery].
     */
    fun filter(
        framework: Framework = Framework.KOTLIN_VIEWS,
        complexity: Complexity? = null,
        selectedTags: Set<String> = emptySet(),
        searchQuery: String = "",
    ): List<SampleItem> {
        val trimmedQuery = searchQuery.trim().lowercase()
        return SAMPLES.filter { sample ->
            val matchesFramework = sample.getActivityForFramework(framework) != null
            val matchesComplexity = complexity == null || sample.complexity == complexity
            val matchesTags = selectedTags.isEmpty() || selectedTags.all { it in sample.tags }
            val matchesQuery = trimmedQuery.isEmpty() ||
                sample.title.lowercase().contains(trimmedQuery) ||
                sample.description.lowercase().contains(trimmedQuery) ||
                sample.category.lowercase().contains(trimmedQuery) ||
                sample.tags.any { it.lowercase().contains(trimmedQuery) } ||
                sample.apiCalls.any { it.lowercase().contains(trimmedQuery) }

            matchesFramework && matchesComplexity && matchesTags && matchesQuery
        }
    }

    /**
     * Finds a [SampleItem] by exact or partial ID, fully-qualified Activity name, or simple class name.
     * Supports CLI and Android Studio launch intents (`-Psample=PlaceAutocompleteActivity`,
     * `-Psample=PlacesUIKit3D`, etc.).
     */
    fun findById(idOrClassName: String?): SampleItem? {
        if (idOrClassName.isNullOrBlank()) return null
        val query = idOrClassName.trim().substringAfter('/')

        // 1. Exact match on id or any framework activity FQCN
        SAMPLES.find {
            it.id.equals(query, ignoreCase = true) ||
                it.kotlinActivity.equals(query, ignoreCase = true) ||
                it.javaActivity.equals(query, ignoreCase = true) ||
                it.composeActivity.equals(query, ignoreCase = true)
        }?.let { return it }

        // 2. Simple class name or suffix match (e.g., "PlaceAutocompleteActivity", "PlacesUIKit3D")
        return SAMPLES.find { sample ->
            sample.id.substringAfterLast('.').equals(query, ignoreCase = true) ||
                sample.kotlinActivity?.substringAfterLast('.')?.equals(query, ignoreCase = true) == true ||
                sample.javaActivity?.substringAfterLast('.')?.equals(query, ignoreCase = true) == true ||
                sample.composeActivity?.substringAfterLast('.')?.equals(query, ignoreCase = true) == true ||
                sample.title.equals(query, ignoreCase = true)
        }
    }
}

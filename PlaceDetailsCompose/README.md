# Place Details Compose Sample

This sample demonstrates how to use the Places SDK for Android's `PlaceDetailsCompactFragment` within a Jetpack Compose-based application. It allows users to tap on a Point of Interest (POI) on a Google Map to display its details in a compact, embedded view.

## Features

- **Jetpack Compose UI**: The entire application is built using Jetpack Compose, showcasing modern Android UI development.
- **Google Maps Integration**: Utilizes the `GoogleMap` composable from the Maps Compose library to display an interactive map.
- **Place Details**: On tapping a POI on the map, the app displays the place's details using the `PlaceDetailsCompactFragment`.
- **MVVM Architecture**: Follows the Model-View-ViewModel pattern, with a `MapViewModel` managing the UI state.
- **Location-Aware**: Requests location permissions to center the map on the user's current location.
- **Secrets Management**: Uses the Secrets Gradle Plugin for Android to securely handle the Google Maps API key.

## Requirements

- Android Studio
- An Android device or emulator
- A Google Maps API key

## Setup and Installation

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/googlemaps-samples/android-places-demos.git
    ```
2.  **Open the project in Android Studio:**
    Open the `PlaceDetailsCompose` directory in Android Studio.

3.  **Add your API Key:**
    -   Create a file named `secrets.properties` in the root directory of the `PlaceDetailsCompose` project (`/Users/dkhawk/AndroidStudioProjects/github-maps-code/android-places-demos/PlaceDetailsCompose`).
    -   Add your Google Maps API key to the `secrets.properties` file, making sure that the Maps SDK for Android and the Places API are enabled for the key. Both `MAPS_API_KEY` and `PLACES_API_KEY` can use the same key.
        ```
        MAPS_API_KEY="YOUR_API_KEY"
        PLACES_API_KEY="YOUR_API_KEY"
        ```
    - Note: The `secrets.properties` file is included in the `.gitignore` file to prevent it from being checked into version control.

## Running the Application

Once the project is set up and the API key is added, you can run the application on an Android device or emulator directly from Android Studio.

- The app will request location permissions.
- The map will center on the user's location if permission is granted, otherwise it will default to Sydney, Australia.
- Tap on any POI on the map to see the Place Details view.

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

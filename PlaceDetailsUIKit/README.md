# **Place Details UI Kit Sample for Android**

This Android application is a demonstration of the PlaceDetailsCompactFragment from the new **Places
UI Kit for Android**. It showcases how to integrate a Google Map, handle user interactions with
Points of Interest (POIs), and display a fully-featured and customizable place details widget.

This sample also demonstrates best practices for handling runtime permissions, the Android Activity
lifecycle (including configuration changes like screen rotation), and lifecycle-aware data loading
to prevent common crashes.

## **Features**

* **Google Map Integration**: Displays an interactive Google Map centered on the user's location.
* **POI Click Handling**: Detects clicks on POIs and retrieves their unique Place ID.
* **Place Details UI Kit**: Uses the modern PlaceDetailsCompactFragment to display rich details
  about a selected place.
* **Dynamic Orientation**: The PlaceDetailsCompactFragment automatically adjusts its layout between
  VERTICAL and HORIZONTAL based on the device's orientation.
* **Robust State Management**: Uses a ViewModel to retain the selected place across configuration
  changes (e.g., screen rotation), ensuring a seamless user experience.
* **Advanced Customization**: Features a custom "Synthwave" theme for the
  PlaceDetailsCompactFragment to demonstrate how easily the widget's appearance can be modified.
* **Lifecycle-Aware Implementation**: Includes a robust solution to prevent common lifecycle-related
  crashes when loading the fragment.

## **Getting Started**

To build and run this sample application, you will need an API key from the Google Cloud Console.

### **1\. Set Up Your API Key**

1. Go to the [Google Cloud Console](https://console.cloud.google.com/).
2. Create a new project or select an existing one.
3. Enable the **Maps SDK for Android** and the **Places API**.
4. Create an API key. For security, it's highly recommended to restrict your API key to your Android
   app's package name and SHA-1 certificate fingerprint.
5. In the root directory of this project, create a file named secrets.properties. This file is
   already listed in .gitignore to prevent it from being checked into version control.
6. Add your API key to the secrets.properties file. The key should be assigned to both
   MAPS\_API\_KEY and PLACES\_API\_KEY:  
   MAPS\_API\_KEY="YOUR\_API\_KEY\_HERE"  
   PLACES\_API\_KEY="YOUR\_API\_KEY\_HERE"

   *Note: The sample uses the same key for both, which is a common practice.*

### **2\. Build and Run**

1. Open the project in Android Studio.
2. Let Gradle sync the project dependencies.
3. Run the app on an Android emulator or a physical device.

The app will request location permissions. Once granted, it will zoom to your current location.
Tapping on any POI on the map (e.g., a restaurant, park, or shop) will display the
PlaceDetailsCompactFragment at the bottom of the screen.

## **Code Highlights**

The primary logic is contained within
app/src/main/java/com/example/placedetailsuikit/MainActivity.kt.

### **MainActivity.kt**

* **MainViewModel**: A simple ViewModel class defined at the top of the file. Its sole purpose is to
  store the selectedPlaceId so that it survives configuration changes.
* **onCreate()**:
    * Initializes the ActivityResultLauncher for handling location permission requests.
    * Initializes the Places SDK and the FusedLocationProviderClient.
    * Crucially, it checks if viewModel.selectedPlaceId is not null. If it has a value (meaning the
      app was rotated while a place was selected), it calls showPlaceDetailsFragment() to restore
      the view.
* **onPoiClick(poi: PointOfInterest)**:
    * This is the callback for when a user taps a POI on the map.
    * It saves the poi.placeId to the viewModel.
    * It then calls showPlaceDetailsFragment() to display the widget.
* **showPlaceDetailsFragment(placeId: String)**:
    * This is the core function for displaying the widget.
    * It dynamically determines the orientation (HORIZONTAL or VERTICAL) based on the device's
      current configuration.
    * It creates a new instance of PlaceDetailsCompactFragment, passing it the content to display,
      the orientation, and a custom theme (R.style.CustomizedPlaceDetailsTheme).
    * It sets a PlaceLoadListener to handle onSuccess and onFailure events. The UI (loading
      indicator and fragment visibility) is updated in these callbacks.
    * It adds the fragment to the FragmentContainerView using the FragmentManager.
    * **Important**: The call to fragment.loadWithPlaceId(placeId) is wrapped in
      binding.root.post { ... }. This is a key fix that prevents a
      kotlin.UninitializedPropertyAccessException crash by ensuring the fragment's view is fully
      created and attached before its data is loaded.

### **Customization**

The custom "Synthwave" theme is defined in app/src/main/res/values/themes.xml and
app/src/main/res/values/colors.xml. By overriding attributes like placesColorSurface,
placesColorPrimary, and placesTextAppearanceBodyMedium, you can completely change the look and feel
of the widget to match your app's branding.

\<\!-- In themes.xml \--\>  
\<style name="CustomizedPlaceDetailsTheme" parent="PlacesMaterialTheme"\>  
\<\!-- Core Colors \--\>  
\<item name="placesColorSurface"\>@color/synthwave\_surface\</item\>  
\<item name="placesColorPrimary"\>@color/synthwave\_primary\</item\>  
...  
\<\!-- Typography \--\>  
\<item name="placesTextAppearanceBodyMedium"\>@style/app\_text\_appearence\_mono\</item\>  
\</style\>

This sample provides a complete and robust foundation for integrating the Places UI Kit into your
own applications.
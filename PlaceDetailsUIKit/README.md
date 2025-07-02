# **Place Details UI Kit Samples for Android**

This Android application provides two distinct demonstrations of the **Places** UI Kit for **Android
**, showcasing how to integrate and customize the PlaceDetailsCompactFragment.

1. **Simple Integration (MainActivity)**: A straightforward example of how to add the Place Details
   widget to an app. It focuses on handling map interactions, displaying the widget with a default
   set of content, and persisting its state across screen rotations using a ViewModel.
2. **Configurable Integration (ConfigurablePlaceDetailsActivity)**: A more advanced example that
   demonstrates how to dynamically configure the content sections displayed within the widget. It
   features a settings dialog, built with Jetpack Compose, that allows the user to select which
   place data fields (e.g., Photos, Rating, Website) they want to see.

Both samples demonstrate best practices for handling runtime permissions, the Android Activity
lifecycle (including configuration changes), and lifecycle-aware data loading to prevent common
crashes.

## **Features**

* **Google Map Integration**: Displays an interactive Google Map centered on the user's location.
* **POI Click Handling**: Detects clicks on POIs and retrieves their unique Place ID.
* **Place Details UI Kit**: Uses the modern PlaceDetailsCompactFragment to display rich details
  about a selected place.
* **Dynamic Orientation**: The PlaceDetailsCompactFragment automatically adjusts its layout between
  VERTICAL and HORIZONTAL based on the device's orientation.
* **Robust State Management**: Uses a ViewModel to retain the selected place and/or configuration
  across configuration changes (e.g., screen rotation), ensuring a seamless user experience.
* **Advanced Customization**: Features a custom "Synthwave" theme for the
  PlaceDetailsCompactFragment to demonstrate how easily the widget's appearance can be modified.
* **Dynamic Content Configuration**: The ConfigurablePlaceDetailsActivity shows how to let users
  choose which Place.Field sections are displayed in the widget at runtime.
* **Jetpack Compose Integration**: The content selection dialog is built using Jetpack
  Compose, showcasing its use within a View-based project.
* **Lifecycle-Aware Implementation**: Includes a robust solution to prevent common lifecycle-related
  crashes when loading the fragment.

## **Getting Started**

To build and run this sample application, you will need an API key from the Google Cloud Console.

### **Set Up Your API Key**

1. Go to the [Google Cloud Console](https://console.cloud.google.com/).
2. Create a new project or select an existing one.
3. Enable the **Maps SDK for Android** and the **Places API**.
4. Create an API key. For security, it's highly recommended to restrict your API key to your Android
   app's package name and SHA-1 certificate fingerprint.
5. In the root directory of this project, create a file named secrets.properties. This file is
   already listed in .gitignore to prevent it from being checked into version control.
6. Add your API key to the secrets.properties file. The key should be assigned to both
   MAPS_API_KEY and PLACES_API_KEY:

```properties
   MAPS_API_KEY="YOUR_API_KEY_HERE"
   PLACES_API_KEY="YOUR_API_KEY_HERE"
```

### **Build and Run**

1. Open the project in Android Studio.
2. Let Gradle sync the project dependencies.
3. Run the app on an Android emulator or a physical device.

The app has two launcher activities. You can choose which one to run using the "Run/Debug
Configurations" dropdown in Android Studio.

* **MainActivity**: Launches the simple, non-configurable demo.
* **ConfigurablePlaceDetailsActivity**: Launches the advanced demo with content selection.

The app will request location permissions. Once granted, it will zoom to your current location.
Tapping on any POI on the map (e.g., a restaurant, park, or shop) will display the
PlaceDetailsCompactFragment at the bottom of the screen. In the configurable demo, a settings icon
allows you to customize the widget's content.

## **Code Highlights**

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

### **ConfigurablePlaceDetailsActivity.kt**

This activity demonstrates a more advanced use case where the content of the widget is
user-configurable.

* **ContentSelectionViewModel.kt**: This ViewModel is more complex. It holds both the
  selectedPlaceId and the state of the content configuration. It uses StateFlow to expose lists of
  selected and unselected content items, which the UI observes.
* **Content Configuration Dialog**:
    * The configure\_button FAB opens an AlertDialog.
    * The dialog's view (content\_selector\_dialog.xml) contains a ComposeView.
    * The UI of the dialog is built declaratively with Jetpack Compose in the DialogContent
      composable function. It displays two lists with sticky headers for "Selected" and "Unselected"
      content.
    * Clicking an item calls viewModel.toggleSelection(), which atomically updates the state flows,
      causing the Compose UI to automatically re-render.
* **showPlaceDetailsFragment(placeId: String)**:
    * This function is similar to the one in MainActivity, but with one key difference.
    * When creating the PlaceDetailsCompactFragment, it gets the list of content directly from the
      ViewModel: PlaceDetailsCompactFragment.newInstance(viewModel.selectedContent.value.map {
      it.content }, ...)
    * This ensures that whatever content the user has selected in the dialog is what the fragment
      will request and display.

### **Customization**

The custom "Synthwave" theme is defined in [`themes.xml`](app/src/main/res/values/themes.xml) and
[`colors.xml`](app/src/main/res/values/colors.xml). By overriding attributes like placesColorSurface,
placesColorPrimary, and placesTextAppearanceBodyMedium, you can completely change the look and feel
of the widget to match your app's branding.

```xml
<!-- In themes.xml -->  
<style name="CustomizedPlaceDetailsTheme" parent="PlacesMaterialTheme">  
<!-- Core Colors -->  
<item name="placesColorSurface">@color/synthwave_surface</item>  
<item name="placesColorPrimary">@color/synthwave_primary</item>  
...  
<!-- Typography -->  
<item name="placesTextAppearanceBodyMedium">@style/app_text_appearence_mono</item>  
</style>
```

This sample provides a complete and robust foundation for integrating the Places UI Kit into your
own applications.
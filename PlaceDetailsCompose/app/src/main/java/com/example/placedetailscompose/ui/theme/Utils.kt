package com.example.placedetailscompose.ui.theme

import android.app.Activity
import android.view.View
import androidx.core.view.WindowCompat

/**
 * Sets the status bar color for the given Activity, handling different API levels and modern
 * best practices to avoid the deprecation warning.
 *
 * @param activity The target Activity.
 * @param color The color to set (e.g., Color.Red.toArgb()).
 * @param isLight True if the status bar content (icons/text) should be dark (for light backgrounds).
 */
fun setStatusBarColor(activity: Activity, color: Int, isLight: Boolean) {
    // Set the color directly on the Window
    // This property is used across many API levels, and while the deprecated method
    // is often the one that causes the linter warning, accessing the property directly
    // is the way to set it in a modern way for pre-API 35 devices.
    @Suppress("DEPRECATION")
    activity.window.statusBarColor = color

    // --- Modern System Insets & Light Status Bar Handling ---

    // 1. Get the WindowInsetsControllerCompat (backward-compatible controller)
    val controller = WindowCompat.getInsetsController(activity.window, activity.findViewById<View>(android.R.id.content))

    // 2. Set the appearance (dark icons/text for light status bar background)
    controller.isAppearanceLightStatusBars = isLight

    // Optional: Ensure the window content is drawn *behind* the status bar
    // This is the core of modern edge-to-edge handling and what the deprecation message suggests.
    WindowCompat.setDecorFitsSystemWindows(activity.window, false)
}

> **Status: historical design doc (Aug 2026).** Kept for context on *why* the app is shaped
> the way it is. Parts have since been superseded — notably persistence: this guide says
> "Use Jetpack DataStore", but the shipped app deliberately uses a single synchronous
> SharedPreferences key instead (a DataStore-based design caused a restart state-divergence
> bug; see `../AGENTS.md` post-mortem #1 and `../README.md`).

# Building Your Door Lock Watch App: A Step-by-Step Guide

This guide will walk you through creating a Wear OS application to track whether you've locked your door, integrated directly into your existing `app` module.

---

### **Phase 1: Manage the Lock State (ViewModel)**

We need a central place to store and manage whether the door is locked.

1.  **Create a ViewModel:** In your `app` module, create a file named `DoorLockViewModel.kt` in the `presentation` directory (next to `MainActivity.kt`).
2.  **Hold the State:** Inside this ViewModel, use a `StateFlow` to hold the lock status. This allows the UI to automatically update when the state changes.

    ```kotlin
    // In DoorLockViewModel.kt
    package com.example.doorlockcheck.presentation

    import androidx.lifecycle.ViewModel
    import kotlinx.coroutines.flow.MutableStateFlow
    import kotlinx.coroutines.flow.StateFlow

    class DoorLockViewModel : ViewModel() {
        private val _isLocked = MutableStateFlow(false) // Default to unlocked
        val isLocked: StateFlow<Boolean> = _isLocked

        fun setLockedStatus(locked: Boolean) {
            _isLocked.value = locked
        }
    }
    ```

---

### **Phase 2: Build the UI (The "Tap")**

Let's integrate this state with your existing `MainActivity.kt`.

1.  **Add Dependencies:** Open the `app/build.gradle.kts` file and ensure you have the `androidx.lifecycle.viewmodel.compose` dependency (often called `lifecycle-viewmodel-compose` in version catalogs) so you can easily obtain your ViewModel in Composable functions.
2.  **Modify the UI:** Open `MainActivity.kt`.
    *   Initialize your `DoorLockViewModel` in `MainActivity` (or inside `WearApp` using `viewModel()`).
    *   Use `collectAsState()` on `viewModel.isLocked` to observe the state within your UI.
    *   Change the UI elements inside `WearApp` (such as the main button or headers) to show the current lock status, and use a button to call `viewModel.setLockedStatus()` when tapped.

---

### **Phase 3: Voice Input (The "Voice")**

1.  **Add Permission:** Open the `AndroidManifest.xml` in your `app` module (`app/src/main/`) and add the permission to use the microphone:
    `<uses-permission android:name="android.permission.RECORD_AUDIO" />`
2.  **Use the Speech Recognizer:** In your `MainActivity.kt`, use `rememberLauncherForActivityResult` with `ActivityResultContracts.StartActivityForResult` and an `Intent` for `RecognizerIntent.ACTION_RECOGNIZE_SPEECH` to capture voice input.
3.  **Process the Result:** When you get the voice result back, check if it contains keywords like "locked" or "unlocked" and update your `DoorLockViewModel` accordingly.

---

### **Phase 4: Geofencing (The "Area")**

This is how the app will know you've left home.

1.  **Add Permissions:** In your `app` module's `AndroidManifest.xml`, add location permissions:
    *   `<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />`
    *   `<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />` (Crucial for checking when the app is not open).
2.  **Request Permissions at Runtime:** In your app, you must explicitly ask the user to grant these permissions.
3.  **Create a Geofence:** Use the `GeofencingClient` to create a `Geofence` object. You'll define a circular area with a latitude, longitude (your home), and a radius (e.g., 100 meters).
4.  **Create a BroadcastReceiver:** This component will listen for geofence events. Create a new class that extends `BroadcastReceiver`. When its `onReceive` method is triggered, it means you have entered or exited the geofence.

---

### **Phase 5: Notifications (The "Shout")**

1.  **Check State in Receiver:** Inside your `BroadcastReceiver`, when you receive a `Geofence.GEOFENCE_TRANSITION_EXIT` event, you need to check if the door is locked.
2.  **Read the State:** Do *not* check the ViewModel here, as the app might not be running. You will read the state directly from the persistence layer (see Phase 6).
3.  **Fire Notification:** If the state is "unlocked," use the `NotificationManagerCompat` to create and display a high-priority notification. Make it noticeable with `setVibrate()` and a custom sound.

---

### **Phase 6: Persistence (Remembering the Lock)**

A ViewModel's state is temporary. You need to save the lock status to a file so it survives app restarts and can be read by your `BroadcastReceiver`.

1.  **Use Jetpack DataStore:** This is the modern Android way to store simple key-value data. Add the `datastore-preferences` dependency to your `app/build.gradle.kts`.
2.  **Save on Change:** In your `DoorLockViewModel`, every time `setLockedStatus` is called, also write the new boolean value to DataStore.
3.  **Read on Start:** When the ViewModel is first created, it should read the last saved value from DataStore to initialize its `_isLocked` state.
4.  **Read in Receiver:** Your `BroadcastReceiver` will also read directly from DataStore to get the current, persisted lock status.

This plan covers the main components of your app. I recommend tackling one phase at a time. Good luck!

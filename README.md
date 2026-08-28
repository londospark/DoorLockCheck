# Door Lock Check (Wear OS)

A Wear OS application to track and record whether the front door has been checked. This app provides a simple UI for manual tracking and exposes AppFunctions for integration with AI agents.

## Features

- **Wear OS UI**: Simple toggle to mark the door as checked/unchecked.
- **Persistence**: Uses Jetpack DataStore to remember the status.
- **AppFunctions**: Exposes `isFrontDoorChecked` and `setFrontDoorChecked` to the Android system, allowing AI agents (like Gemini) to discover and interact with the app.

## Tech Stack

- **Kotlin** & **Jetpack Compose for Wear OS**
- **Jetpack DataStore** (Preferences)
- **AndroidX AppFunctions** (version `1.0.0-alpha10`)
- **KSP** (Kotlin Symbol Processing) for AppFunction metadata generation.

## Key Files

- `app/src/main/java/dev/hubball/doorlockcheck/presentation/DoorStatusAppFunctionService.kt`: Implementation of the AppFunctions.
- `app/src/main/java/dev/hubball/doorlockcheck/presentation/DoorStatusPreferences.kt`: DataStore configuration and keys.
- `app/src/main/res/xml/app_metadata.xml`: High-level description of the app for AI agents.
- `app/src/main/AndroidManifest.xml`: Service registration and metadata linking.

## Setup for Development

### Prerequisites

- Android Studio (latest Canary recommended for Android 16/AppFunctions support).
- A Wear OS emulator or device.

### Project Configuration

- **Namespace**: `dev.hubball.doorlockcheck`
- **Minimum SDK**: 30
- **Target SDK**: 36 (Android 16 preview)
- **Compile SDK**: 37 (Android 16 with Minor API Level 1)

### KSP Configuration

The project uses KSP to generate AppFunction schemas. Ensure that the following is in your `app/build.gradle.kts` if metadata generation issues occur:

```kotlin
ksp {
    arg("appfunctions:aggregateAppFunctions", "true")
}
```

## AI Agent Integration

The app is configured to be discoverable by AI agents. You can verify the functions using ADB:

```bash
adb shell cmd app_function list-app-functions --package dev.hubball.doorlockcheck
```

To execute a function via ADB:

```bash
adb shell cmd app_function execute-app-function \
  --package dev.hubball.doorlockcheck \
  --function 'dev.hubball.doorlockcheck.presentation.DoorStatusAppFunctionService#isFrontDoorChecked'
```

## Notes for the next "Clanker" (AI Assistant)

- The project recently migrated to package `dev.hubball.doorlockcheck`.
- It uses the `@AppFunctionServiceEntryPoint` architecture introduced in `1.0.0-alpha10`.
- The `BaseDoorStatusAppFunctionService` is abstract; KSP generates the concrete `DoorStatusAppFunctionService` (which is registered in the Manifest).
- Ensure `compileSdk` remains at 37 with Minor API Level 1 for full AppFunctions support.

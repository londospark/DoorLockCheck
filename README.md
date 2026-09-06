# Door Lock Check

A Wear OS standalone app to track whether you have locked and checked your front door. Provides a
watch face complication to display the status at a glance.

## Features

- **Status Tracking**: A simple button in the app to toggle the door status between
  checked (locked) and unchecked (unlocked).
- **Complication Support**: A SHORT_TEXT complication to show the status on your watch face,
  with a matching lock icon.
- **Persistence**: State lives in a single SharedPreferences key
  (`door_status_prefs` / `is_front_door_checked`), read synchronously so startup never
  flashes the wrong status.
- **Standalone**: The app runs directly on Wear OS without needing a paired phone app.

## Planned

- **Voice**: Interact with the app via voice command to report the lock status and record
  that the door is locked.
- **Location-awareness**: A reminder to lock your door if you leave home with it unlocked.

## Project Structure

- **Status Management**: The app uses `dev.hubball.doorlockcheck.presentation.DoorStatusPreferences`
  for the persistence constants. `data/DoorLockRepository` is the seam, implemented by
  `DoorLockPreferencesRepository`; `MainActivity` and `DoorLockViewModel` handle the UI state.
- **Complication**: `presentation/DoorLockCheckComplicationDataSourceService` reads state
  fresh per request; UI data is built by `presentation/ComplicationDataFactory`.
- **Milestones**:
  1. **Milestone 1**: Implement core UI and persistence. (Done)
  2. **Milestone 2**: Watch face complication integration. (Done)
  3. **Milestone 3**: Voice interaction. (Planned)
  4. **Milestone 4**: Location-aware reminders. (Planned)

## Building and Running

This is a standard Gradle-based Wear OS application.

### Gradle Tasks

- `./gradlew assembleDebug` builds the debug APK.
- `./gradlew testDebugUnitTest` runs all tests (headless via Robolectric).
- `./gradlew check` runs tests and lint.

### Installation

Install the built debug APK directly on your Wear OS emulator or device with
`adb install app/build/outputs/apk/debug/app-debug.apk`. Add the "Door Lock Check" complication
to any watch face that supports editable complications.

### Dependencies

- AndroidX Wear Compose (Material 3) for the user interface.
- Watch Face Complications Data Source for displaying the lock status on the watch face.

## Testing

All tests run headless (`./gradlew testDebugUnitTest`) — there is no `androidTest` layer on
purpose. The suite follows the testing pyramid: unit tests (ViewModel), integration tests
(Repository + persistence under Robolectric), and UI tests that drive the real
`MainActivity` under Robolectric. See `AGENTS.md` for the rules and history behind this setup.

# Door Lock Check (Wear OS)

A Wear OS application to track and record whether the front door has been checked. This app provides a simple UI for manual tracking and is designed to catch you if you forget.

## Current Status (Done)

- **Wear OS UI**: Simple toggle to mark the door as checked/unchecked.
- **Persistence**: Uses Jetpack DataStore to remember the status across reboots.
- **Package Migration**: Successfully migrated from `com.example` to `dev.hubball.doorlockcheck`.
- **Infrastructure**: Cleaned up experimental "AppFunctions" and voice logic to focus on the core flow.

## Roadmap (To-Do)

- **Phase A: Watch Face Complication**: Implement a tap-to-toggle complication for glanceable status and one-tap updates without opening the app.
- **Phase B: Geofencing**: Add home-exit detection to trigger reminders when leaving the house.
- **Phase C: Proactive Notifications**: Fire a high-priority vibration reminder if the door hasn't been marked as checked when a geofence exit is detected.

## Architectural Notes (For the next Clanker)

- **Status Management**: The app uses `dev.hubball.doorlockcheck.presentation.DoorStatusPreferences` for DataStore keys. The `MainActivity` and `DoorLockViewModel` handle the state.
- **Voice Control**: We investigated Gemini AppFunctions and on-device SpeechRecognizer but abandoned them. Voice is either restricted by the OS or inefficient compared to a one-tap complication. See `voice_and_complication_plan.md` for the post-mortem.
- **Complication Plan**: The plan is to have a one-tap toggle complication. This will require a `ComplicationDataSourceService` that writes to the shared DataStore.

## History of Clanker Assistance

This project has been heavily assisted by "Clankers" (AI agents).
- **Milestone 1**: Implement core UI and DataStore persistence.
- **Milestone 2**: Refactor and migrate package namespace.
- **Milestone 3**: Research and prototype AppFunctions (eventually ripped out in favor of the Complication + Geofence approach).
- **Milestone 4**: Clean up technical debt and establish the current roadmap.

Refer to `voice_and_complication_plan.md` for the detailed logic behind the current direction.

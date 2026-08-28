# Door Status Watch App: Why Voice Doesn't Work, and the Complication + Geofence Plan

This supersedes the voice-related parts of `door_lock_app_plan.md`. It documents why we abandoned Gemini voice control for this app, and lays out the plan for a tap-to-toggle complication plus a geofence-triggered reminder — not yet implemented.

---

## Part 1: Why Gemini voice control doesn't work here

### The app's own plumbing is confirmed working

The Android App Functions platform integration (`BaseDoorStatusAppFunctionService` in
`app/src/main/java/dev/hubball/doorlockcheck/presentation/DoorStatusAppFunctionService.kt`)
is correctly wired end-to-end. Verified directly on a Pixel Watch 5 via ADB:

```
adb shell cmd app_function list-app-functions --package dev.hubball.doorlockcheck
adb shell cmd app_function execute-app-function --package dev.hubball.doorlockcheck \
  --function 'dev.hubball.doorlockcheck.presentation.BaseDoorStatusAppFunctionService#setFrontDoorChecked' \
  --parameters '{"isChecked": true}'
```

Both `isFrontDoorChecked` and `setFrontDoorChecked` register correctly, execute correctly, and
persist state correctly. This is not a bug in the app or its App Functions setup.

### Gemini itself refuses to route these requests

Two separate attempts, both failed for different reasons:

1. **Original naming** (`isDoorLocked` / `setDoorLockStatus`, described as managing "lock
   status"): Gemini explicitly refused, complaining about not having access to home security —
   consistent with Google's own documented policy that Gemini blocks voice control of
   security-category devices/actions (locks, gates, cameras, garage doors) and instead opens the
   Google Home app.

2. **Reframed naming** (`isFrontDoorChecked` / `setFrontDoorChecked`, described as a personal
   checklist/reminder with explicit language that it "doesn't control, lock, unlock, or actuate
   any physical door hardware"): Gemini stopped giving the explicit refusal, and instead replied
   conversationally ("Door checked.") — but a full-buffer `adb logcat -b all` capture taken during
   the exact moment of the voice command showed **no** `app_function` execution, no
   `AppFunctionManagerService` activity, and no process bind/start event for
   `DoorStatusAppFunctionService`. The only process-level activity was routine idle-process
   freezing left over from earlier manual testing. The stored state was also unchanged. In other
   words: Gemini said something that sounded like confirmation without ever calling the app.

Renaming and rewording is not something the app can meaningfully iterate its way around here —
Gemini's routing/classification of "door" + "checked/locked" phrasing sits upstream of the App
Functions layer entirely, and its behavior isn't fully predictable from the app side (a hard
block in one form, a silent no-op in another). Google's own support documentation confirms this
is a deliberate, blanket platform restriction, not a bug:

- ["Control your smart home devices with the Gemini mobile app" — Google Gemini Apps
  Help](https://support.google.com/gemini/answer/15335456?hl=en&co=GENIE.Platform%3DAndroid) —
  "Gemini can't help you with some actions on security devices, like unlocking a door for you...
  You cannot ask Gemini to control your security devices, like gates, cameras, locks, and garage
  doors. In such cases, Gemini will open the Google Home app so you can take the action manually."
- ["Google details 'AppFunctions' that let Gemini use Android apps" —
  9to5Google](https://9to5google.com/2026/02/25/android-appfunctions-gemini/) — background on how
  AppFunctions and Gemini are meant to integrate, and where the current limitations sit.

### Why an on-device (non-Gemini) voice option was also dropped

A fallback was considered: skip Gemini entirely and use Android's on-device `SpeechRecognizer`
directly inside the app (the original Phase 3 of `door_lock_app_plan.md`), triggered by a single
tap to open the app.

Third-party Wear OS apps cannot register a background hotword listener — only the OS-level
assistant (Gemini) has that privilege. So this approach still requires one tap to open the app
before it starts listening. Once a tap is required either way, a single tap-to-toggle button is
strictly better than tap-then-speak: it's faster, has no speech-recognition failure risk (wind,
noise, mumbling), and needs no microphone permission. Voice only would have paid off if it
enabled a fully hands-free flow, and that option doesn't exist for this app on this platform.

**Conclusion: no voice path (Gemini or on-device) is planned. The interaction is a single tap.**

---

## Part 2: The plan — complication/tile + geofence reminder

Goal, restated from the original conversation: the user currently says "door is locked" out loud
as a habit reminder when leaving the house; this works when their partner is present to hear it,
but ADHD means it's unreliable when alone. The replacement needs to (a) let them glance at the
door status at a glance, for personal convenience, and (b) actively catch them if they forget
entirely — since forgetting to check is the actual failure mode, not a lack of a way to check.

### Phase A (new): Watch face complication / tile

- Shows the current `isFrontDoorCheckedKey` state at a glance directly on the watch face — no
  need to open the app.
- Tapping it toggles the state directly (a `PendingIntent` writing to the shared `dataStore` used
  by `DoorLockViewModel` and `DoorStatusAppFunctionService`), so leaving the house is genuinely
  one glance + one tap.
- Requires implementing a `ComplicationDataSourceService` (short-text or
  monochromatic-image type) and registering it in `AndroidManifest.xml`.
- The dependencies for this are already declared (currently unused) in
  `gradle/libs.versions.toml`: `androidx-wear-watchface-complications` and
  `androidx-wear-protolayout` — they just need to be added to `app/build.gradle.kts`'s
  `dependencies {}` block and actually wired up.
- The user will need to add the complication to their watch face manually once it exists (via
  watch face long-press → edit → complication slot), same as any other complication.

### Phase B: Geofencing (carried over from `door_lock_app_plan.md` Phase 4)

- Detects when the user leaves home, so the reminder can fire without needing them to have
  remembered to tap anything at all.
- Rather than hardcoding home coordinates, plan is a one-time "set this as home" action in the
  app UI that captures the current location — avoids handing raw lat/long to the assistant/repo.
- Needs `ACCESS_FINE_LOCATION` and `ACCESS_BACKGROUND_LOCATION` runtime permissions, and a
  `GeofencingClient` + `Geofence` + `BroadcastReceiver` for the `GEOFENCE_TRANSITION_EXIT` event.

### Phase C: Notification (carried over from `door_lock_app_plan.md` Phase 5)

- On geofence exit, read `isFrontDoorCheckedKey` **directly from DataStore** in the
  `BroadcastReceiver` (not via the ViewModel — the app may not be running at that point).
- If the state is "not checked," fire a high-priority notification with vibration — the
  automated equivalent of the partner's out-loud reminder.

### Not doing

- No `SpeechRecognizer` / voice input of any kind (see Part 1).
- No further attempts to route through Gemini App Functions for this flow — the AppFunctions
  service that already exists stays as-is (it's correctly built and still useful for anything
  that isn't classified as security/lock-related), but it is not the mechanism this reminder flow
  will depend on.

---

## Status

Nothing in Part 2 is implemented yet. This document is the plan to build against when the user is
ready to proceed with Phase A (complication), followed by Phases B and C (geofence + notification).

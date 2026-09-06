# Door Lock Check

![CI](https://github.com/londospark/DoorLockCheck/actions/workflows/ci.yml/badge.svg)

A Wear OS standalone app to record whether you have checked and locked your front door, with a
watch face complication so you can see the status at a glance — no more walking back downstairs.

## Features

- **One tap status tracking**: toggle the door between locked and unlocked in the app.
- **Complication**: SHORT_TEXT watch face complication showing `LOCKED` / `OPEN` with a
  matching padlock icon.
- **Instant, honest persistence**: a single SharedPreferences key, read synchronously at
  startup. The UI and the complication share one store, so they cannot disagree — even
  after a restart. (The history behind this choice is in [AGENTS.md](AGENTS.md).)
- **Standalone**: runs entirely on the watch; no phone app required.

## Planned

- Voice interaction (report status / confirm lock without touching the watch).
- Geofenced reminder if you leave home with the door unlocked.

Design notes for these live in [docs/](docs/).

## Building

```bash
./gradlew assembleDebug        # build the debug APK
./gradlew testDebugUnitTest    # run the test suite (headless, Robolectric)
./gradlew check                # tests + lint
```

Install and run:

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

Then add the "Door Lock Check" complication to any watch face with editable complications.

## Project structure

```
app/src/main/java/dev/hubball/doorlockcheck/
  data/
    DoorLockRepository.kt             # persistence seam (synchronous interface)
    DoorLockPreferencesRepository.kt  # the only writer; notifies complications after writes
  presentation/
    DoorStatusPreferences.kt          # the ONE store: file name + key constants
    ComplicationDataFactory.kt        # complication UI data, pure function of (context, isLocked)
    DoorLockCheckComplicationDataSourceService.kt  # reads fresh state per request
    DoorLockViewModel.kt              # thin wrapper over the repository StateFlow
    MainActivity.kt                   # Wear Compose UI
```

## Testing

25 tests, all headless — `./gradlew testDebugUnitTest` is all you need (no emulator):

- **Unit** — `DoorLockViewModelTest`: state logic against a fake repository.
- **Integration** — `DoorLockPreferencesRepositoryTest`: real persistence under Robolectric,
  including the restart-regression test that guards the state-divergence bug.
- **UI** — `MainActivityTest`: drives the real Activity (production factory, repository and
  preferences) and asserts both what's rendered and what's persisted.
- **Complication** — `ComplicationDataFactoryTest`: renders the production complication data
  and asserts text, content description and icon.

There is deliberately no `androidTest` layer; see [AGENTS.md](AGENTS.md) for why.

## Notes for contributors (and AI agents)

[AGENTS.md](AGENTS.md) documents the architecture decisions, testing rules, and several
post-mortems from earlier AI-assisted work on this repo. Read it before changing
persistence, complications, or the test setup.

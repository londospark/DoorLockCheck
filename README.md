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
./gradlew assembleRelease      # build the release APK (see signing notes below)
./gradlew bundleRelease        # build the release AAB - the format Play Store requires
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

## Licence

Source-available, not open-source: [PolyForm Noncommercial License 1.0.0](LICENSE), plus an
additional restriction. Anyone can read, build, modify, and use this for noncommercial purposes
(sideloading it yourself, hobby forks, personal study, etc.) for free. Commercial use, and
publishing it (or a fork of it) to any app store or marketplace at all - even for free - are both
reserved to the copyright holder. See [CONTRIBUTING.md](CONTRIBUTING.md)
before opening a pull request.

## Release & Play Store submission

### CI pipelines

- **`.github/workflows/ci.yml`** runs on every push/PR: `check` (tests + lint), `assembleDebug`,
  and `assembleRelease` (as a build smoke test).
- **`.github/workflows/release.yml`** runs on a `vX.Y.Z` tag push (or manually via
  "Run workflow"): runs the tests, then builds a debug APK, a release APK, and a release AAB,
  uploads all three as workflow artifacts, and (for tag pushes) attaches the APKs to a GitHub
  Release. `versionCode` is set to the GitHub Actions run number (always increasing, as Play
  Store requires) and `versionName` comes from the tag (e.g. `v1.2.0` → `1.2.0`).

### Release signing

The `release` build type signs with a real upload key when one is configured, and **falls back
to the debug key** when it isn't — so `assembleRelease`/`bundleRelease` always produce something
installable, but only a properly-signed build is acceptable for Play Store.

To build a signed release locally:

1. Generate an upload keystore once (keep it and its passwords safe - losing it means you can't
   ship updates to an app already on Play Store under Play App Signing without going through
   Google's key-reset process):
   ```bash
   keytool -genkeypair -v -keystore release-upload-key.jks -alias upload \
     -keyalg RSA -keysize 2048 -validity 10000
   ```
2. Copy `keystore.properties.example` to `keystore.properties` (gitignored) and fill in the
   `storeFile` path plus the passwords/alias you just chose.
3. Run `./gradlew assembleRelease bundleRelease` as normal.

For CI (`release.yml`), signing comes from **GitHub Actions secrets** instead of a local file -
add these under the repo's Settings → Secrets and variables → Actions:

| Secret | Value |
| --- | --- |
| `RELEASE_KEYSTORE_BASE64` | `base64 -w0 release-upload-key.jks` output |
| `RELEASE_KEYSTORE_PASSWORD` | the keystore password |
| `RELEASE_KEY_ALIAS` | the key alias (e.g. `upload`) |
| `RELEASE_KEY_PASSWORD` | the key password |

Once those four secrets exist, tagged release builds are signed with your real upload key
instead of falling back to debug signing.

### Automatic Play Store upload (optional)

`release.yml` has a `deploy-play-store` job that pushes the release AAB straight to the Play
Store **internal testing** track as a draft, using the store listing text/screenshots below.
It only runs once a `PLAY_STORE_SERVICE_ACCOUNT_JSON` secret is set (a Google Play Console
service account JSON key with Release Manager access to this app) - until then it's simply
skipped, and you upload the AAB from a workflow run manually instead.

### Store listing metadata

`fastlane/metadata/android/en-GB/` holds the Play Store listing content, following the layout
`fastlane supply` (and the upload action above) expect: `title.txt`, `short_description.txt`,
`full_description.txt`, `changelogs/<versionCode>.txt` (add a new file here for every release),
plus `images/icon.png` (512×512), `images/featureGraphic.png` (1024×500), and
`images/wearScreenshots/` (captured from the Wear OS emulator).

### Play Console checklist (one-time, manual)

Everything above gets you a signed AAB and listing content. Google Play still requires you to,
in the Play Console itself:

- [ ] Create the app entry (package `dev.hubball.doorlockcheck`) and enrol in Play App Signing.
- [ ] Fill in the **App content** section: privacy policy URL (the app collects no data, but
      Play still requires a URL - a single static page saying so is enough), data safety form,
      content rating questionnaire, target audience, and ads declaration.
- [ ] Set pricing/countries and complete the store listing (the text/images here can be pasted
      in directly, or pushed automatically once `PLAY_STORE_SERVICE_ACCOUNT_JSON` is set).
- [ ] Upload the first release manually once (Play Console requires this before the API can
      push subsequent releases) - use the AAB from a `release.yml` run.

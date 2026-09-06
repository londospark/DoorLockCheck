# AGENTS.md - Working on DoorLockCheck

Guidance for AI agents (and humans) working in this repo. Everything under
"Post-mortems" is a real bug that shipped in this codebase during AI-driven
development. Read them before "helping".

## Commands

```bash
# System java is not on PATH; point Gradle at Android Studio's JBR:
export JAVA_HOME=/home/londo/.local/share/JetBrains/Toolbox/apps/android-studio/jbr

./gradlew assembleDebug            # build the APK
./gradlew testDebugUnitTest        # all tests (headless, Robolectric)
./gradlew check                    # tests + lint
```

CI (`.github/workflows/ci.yml`) runs `testDebugUnitTest` + `assembleDebug` on every push.

## Architecture (as of Sept 2026)

- **One store, one key.** Door state is a single boolean in SharedPreferences
  (`door_status_prefs` / `is_front_door_checked`). Constants live in
  `DoorStatusPreferences.kt`; nothing else may hardcode those strings.
- **Why SharedPreferences and not DataStore:** the app had a restart bug when it
  used DataStore *plus* a SharedPreferences seed (see Post-mortem #1). A single
  boolean does not need an async persistence API. SharedPreferences is a
  per-process singleton, synchronous, and shared by the Activity and the
  complication service (same process) - so state physically cannot diverge.
  Writes are synchronous; `apply()` flushes to disk in the background.
- **Layers:**
  - `data/DoorLockRepository` - the persistence seam. Synchronous on purpose.
  - `data/DoorLockPreferencesRepository` - the only production implementation
    and the only writer. After every write it asks watch faces to refresh the
    complication (best effort; failures are logged, never fatal).
  - `presentation/ComplicationDataFactory` - builds complication UI data.
    Pure-ish function of `(context, isLocked)`, shared by the live complication
    and the editor preview, directly unit-tested.
  - `presentation/DoorLockCheckComplicationDataSourceService` - reads raw prefs
    **fresh on every request** and delegates rendering to the factory. It must
    not cache state in a flow: the app process writes, and a cache here would
    go stale.
  - `presentation/DoorLockViewModel` - thin holder over `repository.isLocked`
    (a `StateFlow`, so `collectAsState()` needs no initial value and startup
    shows the real state, never a flash of wrong data).
  - `presentation/MainActivity` - creates the VM via `by viewModels()` with an
    inline factory. Do **not** replace this with `by lazy` (see Post-mortem #4).
- **No DI framework.** The factories are 5 lines; adding Hilt to this codebase
  is overkill. Test seams are the `DoorLockRepository` interface and the public
  constructors.

## Testing rules

- **JUnit 4 only.** Robolectric requires it, and AGP's default unit-test runner
  is JUnit 4. A JUnit 5 test added here compiles fine and is then *silently
  skipped* by `testDebugUnitTest` unless someone also wires up
  `useJUnitPlatform()` + the vintage engine. This happened (Post-mortem #2).
- **Every test layer must run locally.** There is deliberately no `androidTest`
  layer: the previous one didn't compile and nobody noticed for weeks
  (Post-mortem #3). Robolectric + `createAndroidComposeRule` gives the same
  coverage (real Activity, real repository, real prefs) headless.
  If you re-add instrumented tests, you must also make CI *compile* them.
- **Robolectric + Compose needs:** `@GraphicsMode(GraphicsMode.Mode.NATIVE)` and
  app/test classpaths resolving the *same* compose versions (both get
  `platform(compose-bom)`; pinning core compose versions manually breaks this -
  see Post-mortem #5).
- **When a test fails, fix the code unless the test is provably wrong.** A
  failing test is a signal, not an obstacle. Post-mortem #6 is the cautionary
  tale.
- **Regression tests must be mutation-checked.** The restart test in
  `DoorLockPreferencesRepositoryTest` was verified by reintroducing the
  original bug and confirming the test fails. Do this for any test that guards
  a known bug.
- Robolectric gives each test method a fresh data directory - no cross-test
  cleanup hacks, no cache-reset hooks in production code (Post-mortem #7).
- **No test-only API in production classes** (public cache-clearers,
  `@VisibleForTesting` singleton resets, etc.). Design the seam instead:
  constructors take `Context`, classes don't self-register as singletons.

## Post-mortems - what went wrong here before

1. **Restart state divergence.** Repository state was seeded from SharedPreferences
   but every write went to DataStore. Lock the door, restart the app: UI showed
   "Door is Unlocked" while the complication showed LOCKED. Verified with a
   scratch test in Sept 2026. Lesson: two stores = two sources of truth = a
   sync bug. Trace one real user session (lock → kill → relaunch) through any
   change that touches persistence.
2. **Silently skipped tests.** The suite mixed JUnit 4 (Robolectric) and
   JUnit 5 tests under AGP's JUnit-4-only runner. "22 tests passed" actually
   meant "the 7 tests written in the wrong framework never ran." Lesson: count
   test results (`grep tests= build/test-results/`), don't trust BUILD SUCCESSFUL.
3. **The e2e layer never compiled.** Instrumented Compose tests referenced
   Robolectric (not available on that classpath), called `setContent` on an
   already-launched Activity, and nothing ever ran `assembleDebugAndroidTest`.
   Unexecuted test code is liability, not coverage.
4. **`by lazy` instead of `by viewModels()`.** A ViewModel factory failed to
   compile, and instead of fixing the factory, ViewModelStore wiring was
   removed - silently killing config-change survival and `onCleared()`.
   Never fix a compile error by deleting the architecture.
5. **Dead config / wrong versions.** `libs.versions.toml` pinned core compose
   to `1.6.2` (a Wear-Compose version number copied onto core artifacts). The
   pin was overridden transitively anyway, and when the BOM later pulled tests
   to a different core-compose version than the app, Robolectric crashed with
   `NoSuchFieldError` on R class ids. Versionless entries + one BOM on both
   classpaths is the fix.
6. **Weakening tests to make them pass.** A receiver test failed against
   repository state; the test was rewritten to read the store directly, which
   made it pass while hiding bug #1. If your instinct is to change the
   assertion so it goes green - stop, that assertion just caught a bug.
7. **Test hooks in production code.** `resetDoorDataStoreCache()`/
   `clearAllDataStoreFiles()` existed only to untangle tests, and calling them
   could leave two live DataStore instances over one file (explicitly
   forbidden). Robolectric's per-test data dirs made them unnecessary.
8. **Half-built features shipped as scaffolding.** A `ToggleReceiver` and
   manifest entry existed solely for a complication tap-action that the
   pinned library version doesn't support; only tests sent the broadcast.
   Either finish the feature or delete it - git history is the archive.
9. **Unused cruft from templates.** `WAKE_LOCK` permission, `wear-sdk`
   `useLibrary`/`<uses-library>`, five orphaned strings. Grep for references
   before declaring work done on files these live in.

## Things that look wrong but aren't

- `compileSdk { version = release(37) { minorApiLevel = 1 } }` - intentional
  preview SDK usage; CI installs `platforms;android-37.1`.
- `ComplicationDataSourceService` calling the listener synchronously in
  `onComplicationRequest` is fine - the work is a cached prefs read.
- The complication still refreshes every 300s via the manifest `UPDATE_PERIOD`
  even if `requestUpdateAll()` fails.

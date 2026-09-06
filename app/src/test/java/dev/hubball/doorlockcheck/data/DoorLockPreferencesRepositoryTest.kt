package dev.hubball.doorlockcheck.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dev.hubball.doorlockcheck.presentation.IS_FRONT_DOOR_CHECKED_KEY
import dev.hubball.doorlockcheck.presentation.doorPreferences
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric gives each test method a fresh application data directory, so
 * SharedPreferences always starts empty here.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class DoorLockPreferencesRepositoryTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.doorPreferences().edit().clear().apply()
    }

    @Test
    fun `initial state is false when storage is empty`() {
        val repository = DoorLockPreferencesRepository(context)
        assertFalse(repository.isLocked.value)
    }

    @Test
    fun `setLocked updates the observable state immediately`() {
        val repository = DoorLockPreferencesRepository(context)
        repository.setLocked(true)
        assertTrue(repository.isLocked.value)
    }

    /**
     * Regression test: a previous design seeded in-memory state from
     * SharedPreferences but persisted to DataStore, so after a restart the app
     * showed "unlocked" while the complication showed "LOCKED". Any change to
     * the persistence layer must keep this passing.
     */
    @Test
    fun `locked state survives a simulated app restart`() {
        val repository = DoorLockPreferencesRepository(context)
        repository.setLocked(true)

        // Simulate process death + relaunch: a brand new repository over the
        // same on-disk state must see the persisted value.
        val relaunched = DoorLockPreferencesRepository(context)
        assertTrue("repository did not restore persisted locked state", relaunched.isLocked.value)
    }

    @Test
    fun `unlocked state survives a simulated app restart`() {
        val repository = DoorLockPreferencesRepository(context)
        repository.setLocked(true)
        repository.setLocked(false)

        val relaunched = DoorLockPreferencesRepository(context)
        assertFalse(relaunched.isLocked.value)
    }

    @Test
    fun `setLocked writes the raw preference key other components read`() {
        val repository = DoorLockPreferencesRepository(context)
        repository.setLocked(true)

        val rawValue = context
            .getSharedPreferences("door_status_prefs", Context.MODE_PRIVATE)
            .getBoolean(IS_FRONT_DOOR_CHECKED_KEY, false)
        assertTrue("complication service reads state via raw preferences", rawValue)
    }

    @Test
    fun `toggle from unlocked locks the door and persists`() {
        val repository = DoorLockPreferencesRepository(context)
        repository.toggle()
        assertTrue(repository.isLocked.value)
        assertTrue(DoorLockPreferencesRepository(context).isLocked.value)
    }

    @Test
    fun `toggle from locked unlocks the door and persists`() {
        val repository = DoorLockPreferencesRepository(context)
        repository.setLocked(true)
        repository.toggle()
        assertFalse(repository.isLocked.value)
        assertFalse(DoorLockPreferencesRepository(context).isLocked.value)
    }

    @Test
    fun `setLocked does not throw when complication update is unavailable`() {
        // In Robolectric there is no wearable service to receive the update
        // request; the repository must swallow the failure and still persist.
        val repository = DoorLockPreferencesRepository(context)
        repository.setLocked(true)
        assertTrue(DoorLockPreferencesRepository(context).isLocked.value)
    }
}

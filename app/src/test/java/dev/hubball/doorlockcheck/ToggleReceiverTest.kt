package dev.hubball.doorlockcheck.presentation

import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ApplicationProvider
import dev.hubball.doorlockcheck.data.DoorLockDataStoreRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class ToggleReceiverTest {

    private lateinit var context: Context
    private lateinit var receiver: ToggleReceiver

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        clearAllDataStoreFiles(context)
        receiver = ToggleReceiver()
    }

    @After
    fun cleanup() {
        clearAllDataStoreFiles(context)
    }

    @Test
    fun `onReceive with correct action toggles from false to true`() {
        assertFalse(getDataStoreValue())
        val intent = Intent("dev.hubball.doorlockcheck.TOGGLE_DOOR")
        receiver.onReceive(context, intent)
        assertTrue(getDataStoreValue())
    }

    @Test
    fun `onReceive with correct action toggles from true to false`() {
        setSharedPreferencesValue(true)
        assertTrue(getSharedPreferencesValue())
        val intent = Intent("dev.hubball.doorlockcheck.TOGGLE_DOOR")
        receiver.onReceive(context, intent)
        // After toggle, DataStore should be false
        // SharedPreferences migration will read false from DataStore on next repo creation
        assertFalse(getSharedPreferencesValue())
    }

    @Test
    fun `onReceive with wrong action does nothing`() {
        assertFalse(getDataStoreValue())
        val intent = Intent("some.other.action")
        receiver.onReceive(context, intent)
        assertFalse(getDataStoreValue())
    }

    @Test
    fun `onReceive with null action does nothing`() {
        assertFalse(getDataStoreValue())
        val intent = Intent().apply { action = null }
        receiver.onReceive(context, intent)
        assertFalse(getDataStoreValue())
    }

    @Test
    fun `multiple toggles flip state each time`() {
        val intent = Intent("dev.hubball.doorlockcheck.TOGGLE_DOOR")
        assertFalse(getDataStoreValue())

        receiver.onReceive(context, intent)
        assertTrue(getDataStoreValue())

        receiver.onReceive(context, intent)
        assertFalse(getDataStoreValue())

        receiver.onReceive(context, intent)
        assertTrue(getDataStoreValue())
    }

    private fun getDataStoreValue(): Boolean {
        return runBlocking {
            val dataStore = context.getDoorDataStore()
            dataStore.data.first()[isFrontDoorCheckedKey] ?: false
        }
    }

    private fun setSharedPreferencesValue(value: Boolean) {
        val prefs = context.getSharedPreferences("door_status_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_front_door_checked", value).apply()
    }

    private fun getSharedPreferencesValue(): Boolean {
        val prefs = context.getSharedPreferences("door_status_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("is_front_door_checked", false)
    }
}

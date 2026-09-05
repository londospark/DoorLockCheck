package dev.hubball.doorlockcheck.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dev.hubball.doorlockcheck.presentation.getDoorDataStore
import dev.hubball.doorlockcheck.presentation.isFrontDoorCheckedKey
import dev.hubball.doorlockcheck.presentation.resetDoorDataStoreCache
import dev.hubball.doorlockcheck.presentation.clearAllDataStoreFiles
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class DoorLockDataStoreRepositoryTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        clearAllDataStoreFiles(context)
    }

    @After
    fun cleanup() {
        clearAllDataStoreFiles(context)
    }

    private fun makeRepo(): DoorLockDataStoreRepository {
        resetDoorDataStoreCache(context)
        return DoorLockDataStoreRepository(context)
    }

    @Test
    fun `initial state is false when no data stored`() {
        val repo = makeRepo()
        assertFalse(repo.isLocked.value)
    }

    @Test
    fun `setLocked persists and reads back true`() = runTest {
        val repo = makeRepo()
        repo.setLocked(true)
        assertTrue(repo.isLocked.value)
    }

    @Test
    fun `setLocked persists and reads back false`() = runTest {
        val repo = makeRepo()
        repo.setLocked(true)
        repo.setLocked(false)
        assertFalse(repo.isLocked.value)
    }

    @Test
    fun `toggle flips state correctly`() = runTest {
        val repo = makeRepo()
        repo.setLocked(false)
        repo.toggle()
        assertTrue(repo.isLocked.value)
        repo.toggle()
        assertFalse(repo.isLocked.value)
    }

    @Test
    fun `SharedPreferences migration reads legacy data`() = runTest {
        val prefs = context.getSharedPreferences("door_status_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_front_door_checked", true).apply()
        val repo = makeRepo()
        assertTrue(repo.isLocked.value)
    }

    @Test
    fun `DataStore directly stores and retrieves value`() = runTest {
        val dataStore = context.getDoorDataStore()
        dataStore.edit { it[isFrontDoorCheckedKey] = true }
        val value = dataStore.data.first()[isFrontDoorCheckedKey]
        assertEquals(true, value)
    }
}

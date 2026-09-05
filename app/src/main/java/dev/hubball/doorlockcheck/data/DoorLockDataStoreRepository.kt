package dev.hubball.doorlockcheck.data

import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.edit
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import dev.hubball.doorlockcheck.presentation.DoorLockCheckComplicationDataSourceService
import dev.hubball.doorlockcheck.presentation.getDoorDataStore
import dev.hubball.doorlockcheck.presentation.isFrontDoorCheckedKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DoorLockDataStoreRepository(
    private val context: Context
) : DoorLockRepository {

    private val _isLocked = MutableStateFlow(
        context.getSharedPreferences("door_status_prefs", Context.MODE_PRIVATE)
            .getBoolean("is_front_door_checked", false)
    )
    override val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val dataStore = context.getDoorDataStore()

    override suspend fun setLocked(isLocked: Boolean) {
        _isLocked.value = isLocked

        dataStore.edit { preferences ->
            preferences[isFrontDoorCheckedKey] = isLocked
        }

        notifyComplicationUpdate()
    }

    override suspend fun toggle() {
        setLocked(!_isLocked.value)
    }

    private fun notifyComplicationUpdate() {
        try {
            val componentName = android.content.ComponentName(
                context,
                DoorLockCheckComplicationDataSourceService::class.java
            )
            val requester = ComplicationDataSourceUpdateRequester.create(context, componentName)
            requester.requestUpdateAll()
        } catch (e: Exception) {
            // Complication update failure should not break the toggle
        }
    }
}

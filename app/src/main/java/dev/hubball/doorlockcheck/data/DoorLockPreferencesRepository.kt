package dev.hubball.doorlockcheck.data

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import dev.hubball.doorlockcheck.presentation.DoorLockCheckComplicationDataSourceService
import dev.hubball.doorlockcheck.presentation.doorPreferences
import dev.hubball.doorlockcheck.presentation.isFrontDoorChecked
import dev.hubball.doorlockcheck.presentation.setFrontDoorChecked
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * SharedPreferences-backed implementation. SharedPreferences is a per-process
 * singleton, so every component in this app (UI, complication service) reads the
 * same in-memory copy and can never diverge from disk.
 */
class DoorLockPreferencesRepository(
    context: Context
) : DoorLockRepository {

    private val appContext = context.applicationContext

    private val _isLocked = MutableStateFlow(appContext.doorPreferences().isFrontDoorChecked())
    override val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    override fun setLocked(isLocked: Boolean) {
        _isLocked.value = isLocked
        appContext.doorPreferences().setFrontDoorChecked(isLocked)
        requestComplicationUpdate()
    }

    override fun toggle() {
        setLocked(!_isLocked.value)
    }

    private fun requestComplicationUpdate() {
        try {
            val componentName = ComponentName(
                appContext,
                DoorLockCheckComplicationDataSourceService::class.java
            )
            ComplicationDataSourceUpdateRequester
                .create(appContext, componentName)
                .requestUpdateAll()
        } catch (e: Exception) {
            // Best effort: the complication refreshes on its own schedule regardless,
            // so a failed push here must not break the user's toggle.
            Log.w(TAG, "Complication update request failed", e)
        }
    }

    companion object {
        private const val TAG = "DoorLockCheck"
    }
}

package dev.hubball.doorlockcheck.presentation

import android.app.Application
import android.content.ComponentName
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DoorLockViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = application.applicationContext.getDoorDataStore()
    private val _isLocked = MutableStateFlow(loadInitialValue(application.applicationContext))
    val isLocked: StateFlow<Boolean> = _isLocked

    companion object {
        private fun loadInitialValue(context: Context): Boolean {
            val prefs = context.getSharedPreferences("door_status_prefs", Context.MODE_PRIVATE)
            return prefs.getBoolean("is_front_door_checked", false)
        }
    }

    init {
        viewModelScope.launch {
            val savedState = dataStore.data.first()
            _isLocked.value = savedState[isFrontDoorCheckedKey] ?: false
        }
    }

    fun toggleLockedStatus() {
        setLockedStatus(!isLocked.value)
    }

    fun setLockedStatus(isLocked: Boolean) {
        _isLocked.value = isLocked

        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[isFrontDoorCheckedKey] = isLocked
            }

            val componentName = ComponentName(
                getApplication(),
                DoorLockCheckComplicationDataSourceService::class.java
            )

            val requester = ComplicationDataSourceUpdateRequester.create(
                getApplication(),
                componentName
            )

            requester.requestUpdateAll()
        }
    }
}

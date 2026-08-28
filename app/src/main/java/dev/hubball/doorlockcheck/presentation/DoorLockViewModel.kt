package dev.hubball.doorlockcheck.presentation

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DoorLockViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = application.applicationContext.dataStore
    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked

    init {
        viewModelScope.launch {
            val savedState = dataStore.data.first()
            _isLocked.value = savedState[isFrontDoorCheckedKey] ?: false
        }
    }

    fun setLockedStatus(isLocked: Boolean) {
        _isLocked.value = isLocked

        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[isFrontDoorCheckedKey] = isLocked
            }
        }
    }
}

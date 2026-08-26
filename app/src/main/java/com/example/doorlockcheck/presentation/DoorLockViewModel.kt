package com.example.doorlockcheck.presentation

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore("door_lock_prefs")

class DoorLockViewModel(application: Application) : ViewModel() {
    private val dataStore = application.applicationContext.dataStore
    private val lockKey= booleanPreferencesKey("is_door_locked")
    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked

    init {
        viewModelScope.launch {
            val savedState = dataStore.data.first()
            _isLocked.value = savedState[lockKey] ?: false
        }
    }

    fun setLockedStatus(isLocked: Boolean) {
        _isLocked.value = isLocked

        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[lockKey] = isLocked
            }
        }
    }
}
package com.example.doorlockcheck.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DoorLockViewModel : ViewModel() {
    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked

    fun setLockedStatus(isLocked: Boolean) {
        _isLocked.value = isLocked
    }
}
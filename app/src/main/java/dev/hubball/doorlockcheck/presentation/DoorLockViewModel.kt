package dev.hubball.doorlockcheck.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.hubball.doorlockcheck.data.DoorLockRepository
import kotlinx.coroutines.launch

class DoorLockViewModel(
    private val repository: DoorLockRepository
) : ViewModel() {
    val isLocked = repository.isLocked

    fun toggleLockedStatus() {
        viewModelScope.launch {
            repository.toggle()
        }
    }

    fun setLockedStatus(isLocked: Boolean) {
        viewModelScope.launch {
            repository.setLocked(isLocked)
        }
    }
}

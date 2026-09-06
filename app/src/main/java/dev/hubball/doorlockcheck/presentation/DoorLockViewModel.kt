package dev.hubball.doorlockcheck.presentation

import androidx.lifecycle.ViewModel
import dev.hubball.doorlockcheck.data.DoorLockRepository
import kotlinx.coroutines.flow.StateFlow

/**
 * Thin UI state holder. No coroutines needed: the repository is synchronous,
 * and state flows out through its StateFlow immediately, so there is nothing
 * to launch.
 */
class DoorLockViewModel(
    private val repository: DoorLockRepository
) : ViewModel() {
    val isLocked: StateFlow<Boolean> = repository.isLocked

    fun toggleLockedStatus() = repository.toggle()

    fun setLockedStatus(isLocked: Boolean) = repository.setLocked(isLocked)
}

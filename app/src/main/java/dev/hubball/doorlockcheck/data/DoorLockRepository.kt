package dev.hubball.doorlockcheck.data

import kotlinx.coroutines.flow.StateFlow

/**
 * Persistence seam for door lock state. Exposed as [StateFlow] so UI (and tests)
 * can observe changes; seeded synchronously from the backing store at construction.
 *
 * Deliberately synchronous: writes are cheap (a key in SharedPreferences memory
 * cache flushed via apply()). Keeping it non-suspend means callers cannot forget
 * to await a write that has already happened.
 */
interface DoorLockRepository {
    val isLocked: StateFlow<Boolean>

    fun setLocked(isLocked: Boolean)

    fun toggle()
}

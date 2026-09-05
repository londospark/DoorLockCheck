package dev.hubball.doorlockcheck.data

import kotlinx.coroutines.flow.Flow

interface DoorLockRepository {
    val isLocked: Flow<Boolean>
    suspend fun setLocked(isLocked: Boolean)
    suspend fun toggle()
}

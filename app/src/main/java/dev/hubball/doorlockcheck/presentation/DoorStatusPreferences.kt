package dev.hubball.doorlockcheck.presentation

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

private const val DOOR_STATUS_PREFERENCES_NAME = "door_status_prefs"

val Context.dataStore by preferencesDataStore(DOOR_STATUS_PREFERENCES_NAME)

val isFrontDoorCheckedKey = booleanPreferencesKey("is_front_door_checked")

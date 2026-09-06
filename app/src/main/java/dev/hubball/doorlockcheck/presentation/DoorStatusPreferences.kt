package dev.hubball.doorlockcheck.presentation

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Single source of truth for door lock state: one SharedPreferences file, one key.
 *
 * History note: this app previously used Jetpack DataStore with a SharedPreferences seed,
 * which created two sources of truth and a state-divergence bug on restart. See AGENTS.md.
 * Do not reintroduce a second store without making one of them a strict write-through cache.
 */
const val DOOR_STATUS_PREFERENCES_NAME = "door_status_prefs"

const val IS_FRONT_DOOR_CHECKED_KEY = "is_front_door_checked"

fun Context.doorPreferences(): SharedPreferences =
    getSharedPreferences(DOOR_STATUS_PREFERENCES_NAME, Context.MODE_PRIVATE)

fun SharedPreferences.isFrontDoorChecked(): Boolean =
    getBoolean(IS_FRONT_DOOR_CHECKED_KEY, false)

fun SharedPreferences.setFrontDoorChecked(isLocked: Boolean) {
    edit { putBoolean(IS_FRONT_DOOR_CHECKED_KEY, isLocked) }
}

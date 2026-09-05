package dev.hubball.doorlockcheck.presentation

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.SharedPreferencesMigration
import okio.Path.Companion.toPath
import java.util.WeakHashMap

private const val DOOR_STATUS_PREFERENCES_NAME = "door_status_prefs"

private val dataStoreMap = WeakHashMap<Context, DataStore<Preferences>>()

fun Context.getDoorDataStore(): DataStore<Preferences> {
    return dataStoreMap[this] ?: PreferenceDataStoreFactory.createWithPath(
        produceFile = { filesDir.resolve(DOOR_STATUS_PREFERENCES_NAME).absolutePath.toPath() },
        migrations = listOf(SharedPreferencesMigration(this, DOOR_STATUS_PREFERENCES_NAME))
    ).also { dataStoreMap[this] = it }
}

val isFrontDoorCheckedKey = booleanPreferencesKey("is_front_door_checked")

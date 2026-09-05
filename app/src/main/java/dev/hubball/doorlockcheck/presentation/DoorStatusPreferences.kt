package dev.hubball.doorlockcheck.presentation

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.SharedPreferencesMigration
import okio.Path.Companion.toPath

private const val DOOR_STATUS_PREFERENCES_NAME = "door_status_prefs.preferences_pb"
private const val SHARED_PREFS_NAME = "door_status_prefs"

private val dataStoreMap = java.util.WeakHashMap<Context, DataStore<Preferences>>()

fun Context.getDoorDataStore(): DataStore<Preferences> {
    return dataStoreMap[this] ?: PreferenceDataStoreFactory.createWithPath(
        produceFile = { filesDir.resolve(DOOR_STATUS_PREFERENCES_NAME).absolutePath.toPath() },
        migrations = listOf(SharedPreferencesMigration(this, SHARED_PREFS_NAME))
    ).also { dataStoreMap[this] = it }
}

fun resetDoorDataStoreCache(context: Context) {
    dataStoreMap.remove(context)
}

fun clearAllDataStoreFiles(context: Context) {
    val file = context.filesDir.resolve(DOOR_STATUS_PREFERENCES_NAME)
    if (file.exists()) file.delete()
    val prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().clear().apply()
    resetDoorDataStoreCache(context)
}

val isFrontDoorCheckedKey = booleanPreferencesKey("is_front_door_checked")

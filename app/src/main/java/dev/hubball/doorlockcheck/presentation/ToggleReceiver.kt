package dev.hubball.doorlockcheck.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class ToggleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "dev.hubball.doorlockcheck.TOGGLE_DOOR") {
            val dataStore = context.getDoorDataStore()
            runBlocking {
                val current = dataStore.data.first()[isFrontDoorCheckedKey] ?: false
                dataStore.edit { prefs ->
                    prefs[isFrontDoorCheckedKey] = !current
                }
            }
        }
    }
}

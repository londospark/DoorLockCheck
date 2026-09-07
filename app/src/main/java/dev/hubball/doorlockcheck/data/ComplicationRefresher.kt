package dev.hubball.doorlockcheck.data

import android.content.ComponentName
import android.content.Context
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import dev.hubball.doorlockcheck.presentation.DoorLockCheckComplicationDataSourceService

/**
 * Seam over the real Wear OS push-update API. Robolectric has no wearable host
 * to receive a real update request, so DoorLockPreferencesRepositoryTest injects
 * a fake to verify a refresh is actually requested on every write - the app-side
 * half of issue #7 (complication showing stale state) that a headless test can
 * reach. Whether the system host actually re-renders after receiving the
 * request is not something this test suite can verify; that needs a real device
 * (see the issue for a confirmed repro).
 */
interface ComplicationRefresher {
    fun refresh()
}

class SystemComplicationRefresher(private val context: Context) : ComplicationRefresher {
    override fun refresh() {
        val componentName = ComponentName(context, DoorLockCheckComplicationDataSourceService::class.java)
        ComplicationDataSourceUpdateRequester.create(context, componentName).requestUpdateAll()
    }
}

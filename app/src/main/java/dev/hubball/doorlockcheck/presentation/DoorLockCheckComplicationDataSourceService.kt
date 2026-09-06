package dev.hubball.doorlockcheck.presentation

import android.util.Log
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import androidx.wear.watchface.complications.datasource.ComplicationRequest

/**
 * Provides door status to watch faces.
 *
 * Reads state straight from SharedPreferences (fresh on every request) rather than
 * caching it in a flow: watch faces pull this service on their own schedule, so a
 * cached value here could go stale whenever the app process writes a new one.
 */
class DoorLockCheckComplicationDataSourceService : ComplicationDataSourceService() {

    override fun onComplicationRequest(
        request: ComplicationRequest,
        listener: ComplicationRequestListener
    ) {
        Log.d(TAG, "onComplicationRequest: ${request.complicationType}")
        val isLocked = applicationContext.doorPreferences().isFrontDoorChecked()

        val data: ComplicationData? = when (request.complicationType) {
            ComplicationType.SHORT_TEXT -> ComplicationDataFactory.shortText(this, isLocked)
            else -> null
        }
        listener.onComplicationData(data)
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        Log.d(TAG, "getPreviewData: requested type: $type")
        return when (type) {
            ComplicationType.SHORT_TEXT -> ComplicationDataFactory.preview(this)
            else -> {
                Log.d(TAG, "Unsupported type requested: $type")
                null
            }
        }
    }

    companion object {
        private const val TAG = "DoorLockCheck"
    }
}

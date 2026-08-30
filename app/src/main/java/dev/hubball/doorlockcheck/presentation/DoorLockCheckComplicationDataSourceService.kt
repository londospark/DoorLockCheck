package dev.hubball.doorlockcheck.presentation

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import kotlinx.coroutines.flow.first

class DoorLockCheckComplicationDataSourceService : SuspendingComplicationDataSourceService() {

    private val dataStore by lazy { applicationContext.dataStore }
    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val savedState = dataStore.data.first()
        val isLocked = savedState[isFrontDoorCheckedKey] ?: false

        return if (isLocked) {
            shortTextComplicationData("Locked")
        } else {
            shortTextComplicationData("Unlocked")
        }
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return shortTextComplicationData("Preview")
    }

    private fun shortTextComplicationData(text: String) =
        ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text).build(),
            contentDescription = PlainComplicationText.Builder(text).build()
        ).build()
}
package dev.hubball.doorlockcheck.presentation

import android.content.Intent
import android.util.Log
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import dev.hubball.doorlockcheck.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DoorLockCheckComplicationDataSourceService : ComplicationDataSourceService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val dataStore by lazy { applicationContext.getDoorDataStore() }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onComplicationRequest(
        request: ComplicationRequest,
        listener: ComplicationDataSourceService.ComplicationRequestListener
    ) {
        Log.d("DoorLockCheck", "onComplicationRequest: ${request.complicationType}")
        serviceScope.launch {
            val savedState = dataStore.data.first()
            val isLocked = savedState[isFrontDoorCheckedKey] ?: false

            val data = when (request.complicationType) {
                ComplicationType.SHORT_TEXT -> shortTextComplicationData(isLocked)
                else -> null
            }
            listener.onComplicationData(data)
        }
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        Log.d("DoorLockCheck", "getPreviewData: requested type: $type")
        
        return when (type) {
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("LOCKED").build(),
                    contentDescription = PlainComplicationText.Builder("Door Status").build()
                )
                .setMonochromaticImage(
                    MonochromaticImage.Builder(
                        android.graphics.drawable.Icon.createWithResource(this, R.drawable.ic_complication_lock)
                    ).build()
                )
                .build()
            }
            else -> {
                Log.d("DoorLockCheck", "Unsupported type requested!")
                null
            }
        }
    }

    private fun shortTextComplicationData(isLocked: Boolean): ShortTextComplicationData {
        val status = if (isLocked) "LOCKED" else "OPEN"
        val iconRes = if (isLocked) R.drawable.ic_complication_lock else R.drawable.ic_complication_unlock
        
        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(status).build(),
            contentDescription = PlainComplicationText.Builder("Door $status").build()
        )
        .setMonochromaticImage(
            MonochromaticImage.Builder(
                android.graphics.drawable.Icon.createWithResource(this, iconRes)
            ).build()
        )
        .build()
    }
}

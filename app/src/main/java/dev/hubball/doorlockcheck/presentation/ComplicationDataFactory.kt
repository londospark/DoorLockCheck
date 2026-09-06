package dev.hubball.doorlockcheck.presentation

import android.content.Context
import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import dev.hubball.doorlockcheck.R

/**
 * Single builder for this app's complication data, shared by the live complication
 * and the watch face editor preview.
 *
 * Extracted from DoorLockCheckComplicationDataSourceService so the text/icon/content
 * description logic is testable without instantiating the service (the service's
 * private helpers previously left that logic untested).
 */
object ComplicationDataFactory {

    const val LOCKED_TEXT = "LOCKED"
    const val OPEN_TEXT = "OPEN"

    fun shortText(context: Context, isLocked: Boolean): ShortTextComplicationData {
        val status = if (isLocked) LOCKED_TEXT else OPEN_TEXT
        val iconRes = if (isLocked) {
            R.drawable.ic_complication_lock
        } else {
            R.drawable.ic_complication_unlock
        }

        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(status).build(),
            contentDescription = PlainComplicationText.Builder("Door $status").build()
        )
            .setMonochromaticImage(
                MonochromaticImage.Builder(
                    Icon.createWithResource(context, iconRes)
                ).build()
            )
            .build()
    }

    /** Preview shown while the user picks a complication; defaults to the locked state. */
    fun preview(context: Context): ShortTextComplicationData = shortText(context, isLocked = true)
}

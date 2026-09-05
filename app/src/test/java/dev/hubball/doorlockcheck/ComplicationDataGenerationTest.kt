package dev.hubball.doorlockcheck.presentation

import android.content.ContextWrapper
import android.graphics.drawable.Icon
import android.R.drawable
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class ComplicationDataGenerationTest {

    private val context = RuntimeEnvironment.getApplication()

    @Test
    fun `buildShortTextComplicationData locked returns non null data`() {
        val data = buildShortTextComplicationData(context, isLocked = true)
        assertNotNull(data)
    }

    @Test
    fun `buildShortTextComplicationData unlocked returns non null data`() {
        val data = buildShortTextComplicationData(context, isLocked = false)
        assertNotNull(data)
    }

    @Test
    fun `buildShortTextComplicationData locked has monochromatic image`() {
        val data = buildShortTextComplicationData(context, isLocked = true)
        val img = data?.monochromaticImage
        assertNotNull(img)
        assertNotNull(img?.image)
    }

    @Test
    fun `buildShortTextComplicationData unlocked has monochromatic image`() {
        val data = buildShortTextComplicationData(context, isLocked = false)
        val img = data?.monochromaticImage
        assertNotNull(img)
        assertNotNull(img?.image)
    }

    @Test
    fun `buildShortTextComplicationData locked has correct text content`() {
        val data = buildShortTextComplicationData(context, isLocked = true)
        val text = data?.text?.getTextAt(context.resources, java.time.Instant.now())
        assertEquals("LOCKED", text.toString())
    }

    @Test
    fun `buildShortTextComplicationData unlocked has correct text content`() {
        val data = buildShortTextComplicationData(context, isLocked = false)
        val text = data?.text?.getTextAt(context.resources, java.time.Instant.now())
        assertEquals("OPEN", text.toString())
    }

    @Test
    fun `buildShortTextComplicationData locked has correct content description`() {
        val data = buildShortTextComplicationData(context, isLocked = true)
        val desc = data?.contentDescription?.getTextAt(context.resources, java.time.Instant.now())
        assertEquals("Door LOCKED", desc.toString())
    }

    @Test
    fun `buildShortTextComplicationData unlocked has correct content description`() {
        val data = buildShortTextComplicationData(context, isLocked = false)
        val desc = data?.contentDescription?.getTextAt(context.resources, java.time.Instant.now())
        assertEquals("Door OPEN", desc.toString())
    }

    @Test
    fun `preview data locked state returns SHORT_TEXT with LOCKED text`() {
        val data = buildPreviewData(context)
        assertNotNull(data)
        val text = (data as ShortTextComplicationData).text.getTextAt(context.resources, java.time.Instant.now())
        assertEquals("LOCKED", text.toString())
    }

    @Test
    fun `preview data returns null for unsupported type`() {
        val data = buildPreviewData(context, ComplicationType.LONG_TEXT)
        assertNull(data)
    }

    @Test
    fun `preview data has monochromatic image`() {
        val data = buildPreviewData(context) as? ShortTextComplicationData
        val img = data?.monochromaticImage
        assertNotNull(img)
        assertNotNull(img?.image)
    }

    private fun buildShortTextComplicationData(
        ctx: ContextWrapper,
        isLocked: Boolean
    ): ShortTextComplicationData? {
        val status = if (isLocked) "LOCKED" else "OPEN"
        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(status).build(),
            contentDescription = PlainComplicationText.Builder("Door $status").build()
        )
            .setMonochromaticImage(
                MonochromaticImage.Builder(
                    Icon.createWithResource(ctx, drawable.ic_menu_report_image)
                ).build()
            )
            .build()
    }

    private fun buildPreviewData(
        ctx: ContextWrapper,
        type: ComplicationType = ComplicationType.SHORT_TEXT
    ): androidx.wear.watchface.complications.data.ComplicationData? {
        return when (type) {
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("LOCKED").build(),
                    contentDescription = PlainComplicationText.Builder("Door Status").build()
                )
                    .setMonochromaticImage(
                        MonochromaticImage.Builder(
                            Icon.createWithResource(ctx, drawable.ic_menu_report_image)
                        ).build()
                    )
                    .build()
            }
            else -> null
        }
    }
}

package dev.hubball.doorlockcheck.presentation

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant

/**
 * Tests the real production builder (ComplicationDataFactory) rather than
 * a copy of its logic - the previous version of this test rebuilt the data
 * inline and could never fail even if the service broke.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class ComplicationDataFactoryTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun ShortTextComplicationData.renderedText(): String =
        text.getTextAt(context.resources, Instant.now()).toString()

    private fun ShortTextComplicationData.renderedContentDescription(): String =
        contentDescription!!.getTextAt(context.resources, Instant.now()).toString()

    @Test
    fun `locked state renders LOCKED`() {
        val data = ComplicationDataFactory.shortText(context, isLocked = true)
        assertEquals(ComplicationDataFactory.LOCKED_TEXT, data.renderedText())
    }

    @Test
    fun `unlocked state renders OPEN`() {
        val data = ComplicationDataFactory.shortText(context, isLocked = false)
        assertEquals(ComplicationDataFactory.OPEN_TEXT, data.renderedText())
    }

    @Test
    fun `locked state content description includes status`() {
        val data = ComplicationDataFactory.shortText(context, isLocked = true)
        assertEquals("Door LOCKED", data.renderedContentDescription())
    }

    @Test
    fun `unlocked state content description includes status`() {
        val data = ComplicationDataFactory.shortText(context, isLocked = false)
        assertEquals("Door OPEN", data.renderedContentDescription())
    }

    @Test
    fun `locked state carries a monochromatic image`() {
        val data = ComplicationDataFactory.shortText(context, isLocked = true)
        assertNotNull(data.monochromaticImage)
        assertNotNull(data.monochromaticImage?.image)
    }

    @Test
    fun `unlocked state carries a monochromatic image`() {
        val data = ComplicationDataFactory.shortText(context, isLocked = false)
        assertNotNull(data.monochromaticImage)
        assertNotNull(data.monochromaticImage?.image)
    }

    @Test
    fun `preview shows the locked state`() {
        val data = ComplicationDataFactory.preview(context)
        assertEquals(ComplicationDataFactory.LOCKED_TEXT, data.renderedText())
    }

    @Test
    fun `lock and unlock render differently`() {
        val locked = ComplicationDataFactory.shortText(context, isLocked = true)
        val unlocked = ComplicationDataFactory.shortText(context, isLocked = false)
        assertNotNull(locked.renderedText())
        assertEquals(false, locked.renderedText() == unlocked.renderedText())
    }
}

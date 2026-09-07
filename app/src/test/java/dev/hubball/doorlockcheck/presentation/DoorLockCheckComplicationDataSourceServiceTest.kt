package dev.hubball.doorlockcheck.presentation

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class DoorLockCheckComplicationDataSourceServiceTest {

    private lateinit var context: Context
    private lateinit var service: DoorLockCheckComplicationDataSourceService

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.doorPreferences().edit().clear().apply()

        val controller = Robolectric.buildService(DoorLockCheckComplicationDataSourceService::class.java)
        service = controller.create().get()
    }

    private fun captureComplicationData(
        request: ComplicationRequest
    ): ComplicationData? {
        var receivedData: ComplicationData? = null
        val listener = object : ComplicationDataSourceService.ComplicationRequestListener {
            override fun onComplicationData(data: ComplicationData?) {
                receivedData = data
            }
        }
        service.onComplicationRequest(request, listener)
        return receivedData
    }

    private fun getTextAt(data: ShortTextComplicationData): String =
        data.text.getTextAt(context.resources, Instant.now()).toString()

    @Test
    fun `onComplicationRequest returns OPEN when door is unlocked`() {
        context.doorPreferences().setFrontDoorChecked(false)
        val request = ComplicationRequest(0, ComplicationType.SHORT_TEXT)

        val data = captureComplicationData(request) as? ShortTextComplicationData
        assertNotNull("Listener should receive data", data)
        assertEquals("Should return OPEN when unlocked", ComplicationDataFactory.OPEN_TEXT,
            getTextAt(data!!))
    }

    @Test
    fun `onComplicationRequest returns LOCKED when door is locked`() {
        context.doorPreferences().setFrontDoorChecked(true)
        val request = ComplicationRequest(0, ComplicationType.SHORT_TEXT)

        val data = captureComplicationData(request) as? ShortTextComplicationData
        assertNotNull("Listener should receive data", data)
        assertEquals("Should return LOCKED when locked", ComplicationDataFactory.LOCKED_TEXT,
            getTextAt(data!!))
    }

    @Test
    fun `onComplicationRequest returns null for unsupported complication type`() {
        context.doorPreferences().setFrontDoorChecked(true)
        val request = ComplicationRequest(0, ComplicationType.LONG_TEXT)

        val data = captureComplicationData(request)
        assertNull("Should return null for unsupported type", data)
    }

    @Test
    fun `getPreviewData returns locked state preview`() {
        val preview = service.getPreviewData(ComplicationType.SHORT_TEXT)
        assertNotNull("Preview should not be null", preview)
        assertEquals("Preview should show LOCKED", ComplicationDataFactory.LOCKED_TEXT,
            getTextAt(preview as ShortTextComplicationData))
    }

    @Test
    fun `getPreviewData returns null for unsupported type`() {
        val preview = service.getPreviewData(ComplicationType.LONG_TEXT)
        assertNull("Preview should be null for unsupported type", preview)
    }

    @Test
    fun `onComplicationRequest reads fresh state on every call from same instance`() {
        // Critical regression test for issue #7: verify the service reads fresh state
        // from SharedPreferences on each call and does not cache. The class's own
        // doc comment promises "reads raw prefs fresh on every request". If someone
        // "optimizes" by caching state in a flow, this test catches it.
        val request = ComplicationRequest(0, ComplicationType.SHORT_TEXT)

        // First request: door is unlocked
        context.doorPreferences().setFrontDoorChecked(false)
        var data = captureComplicationData(request) as? ShortTextComplicationData
        assertNotNull("First request should receive data", data)
        assertEquals("First request should show OPEN", ComplicationDataFactory.OPEN_TEXT,
            getTextAt(data!!))

        // Change state in SharedPreferences without recreating the service instance
        context.doorPreferences().setFrontDoorChecked(true)

        // Second request on same instance: should immediately see new state
        data = captureComplicationData(request) as? ShortTextComplicationData
        assertNotNull("Second request should receive data", data)
        assertEquals("Second request should show LOCKED (fresh read)", ComplicationDataFactory.LOCKED_TEXT,
            getTextAt(data!!))

        // Change state again
        context.doorPreferences().setFrontDoorChecked(false)

        // Third request: should reflect the latest change
        data = captureComplicationData(request) as? ShortTextComplicationData
        assertNotNull("Third request should receive data", data)
        assertEquals("Third request should show OPEN again (fresh read)", ComplicationDataFactory.OPEN_TEXT,
            getTextAt(data!!))
    }
}

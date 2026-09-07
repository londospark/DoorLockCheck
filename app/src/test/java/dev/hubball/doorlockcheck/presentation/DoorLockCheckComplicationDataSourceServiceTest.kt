package dev.hubball.doorlockcheck.presentation

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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

    @Test
    fun `onComplicationRequest returns OPEN when door is unlocked`() {
        context.doorPreferences().setFrontDoorChecked(false)

        var receivedData: ComplicationData? = null
        val listener = object : ComplicationDataSourceService.ComplicationRequestListener {
            override fun onComplicationData(data: ComplicationData?) {
                receivedData = data
            }
        }

        val request = ComplicationRequest(0, ComplicationType.SHORT_TEXT)

        service.onComplicationRequest(request, listener)

        assertNotNull("Listener should receive data", receivedData)
        assertEquals("Should return OPEN when unlocked", ComplicationDataFactory.OPEN_TEXT,
            (receivedData as? androidx.wear.watchface.complications.data.ShortTextComplicationData)?.text?.getTextAt(context.resources, Instant.now()).toString())
    }

    @Test
    fun `onComplicationRequest returns LOCKED when door is locked`() {
        context.doorPreferences().setFrontDoorChecked(true)

        var receivedData: ComplicationData? = null
        val listener = object : ComplicationDataSourceService.ComplicationRequestListener {
            override fun onComplicationData(data: ComplicationData?) {
                receivedData = data
            }
        }

        val request = ComplicationRequest(0, ComplicationType.SHORT_TEXT)

        service.onComplicationRequest(request, listener)

        assertNotNull("Listener should receive data", receivedData)
        assertEquals("Should return LOCKED when locked", ComplicationDataFactory.LOCKED_TEXT,
            (receivedData as? androidx.wear.watchface.complications.data.ShortTextComplicationData)?.text?.getTextAt(context.resources, Instant.now()).toString())
    }
}

package dev.hubball.doorlockcheck

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.hubball.doorlockcheck.presentation.MainActivity
import dev.hubball.doorlockcheck.presentation.doorPreferences
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Drives the real MainActivity: production ViewModel factory, real repository,
 * real preferences. This is the layer that would have caught the restart
 * state-divergence bug end to end. Runs headless under Robolectric, unlike the
 * previous androidTest version which never even compiled (see AGENTS.md).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [30])
class MainActivityTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val context: Context get() = composeRule.activity

    @Test
    fun `launch shows unlocked state and lock action`() {
        composeRule.onNodeWithText("Door is Unlocked").assertIsDisplayed()
        composeRule.onNodeWithText("Locked the Door").assertIsDisplayed()
    }

    @Test
    fun `tapping lock updates status, action, and persisted state`() {
        composeRule.onNodeWithText("Locked the Door").performClick()

        composeRule.onNodeWithText("Door is Locked").assertIsDisplayed()
        composeRule.onNodeWithText("Unlocked the Door").assertIsDisplayed()

        assertTrue(
            "tap must persist immediately so the complication and next launch agree",
            context.doorPreferences().getBoolean("is_front_door_checked", false)
        )
    }

    @Test
    fun `tapping unlock returns to unlocked state`() {
        composeRule.onNodeWithText("Locked the Door").performClick()
        composeRule.onNodeWithText("Unlocked the Door").performClick()

        composeRule.onNodeWithText("Door is Unlocked").assertIsDisplayed()
        composeRule.onNodeWithText("Locked the Door").assertIsDisplayed()
        assertFalse(context.doorPreferences().getBoolean("is_front_door_checked", false))
    }
}

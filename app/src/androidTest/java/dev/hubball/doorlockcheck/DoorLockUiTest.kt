package dev.hubball.doorlockcheck

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.hubball.doorlockcheck.presentation.DoorLockViewModel
import dev.hubball.doorlockcheck.presentation.MainActivity
import dev.hubball.doorlockcheck.presentation.WearApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class DoorLockUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `ListHeader displays door is unlocked when not locked`() {
        composeRule.setContent {
            val fakeRepo = FakeDoorLockRepository()
            val viewModel = DoorLockViewModel(fakeRepo)
            WearApp(viewModel)
        }

        composeRule.onNodeWithText("Door is Unlocked").assertIsDisplayed()
    }

    @Test
    fun `ListHeader displays door is locked when locked`() {
        composeRule.setContent {
            val fakeRepo = FakeDoorLockRepository()
            fakeRepo.setState(true)
            val viewModel = DoorLockViewModel(fakeRepo)
            WearApp(viewModel)
        }

        composeRule.onNodeWithText("Door is Locked").assertIsDisplayed()
    }

    @Test
    fun `Button displays Locked the Door when unlocked`() {
        composeRule.setContent {
            val fakeRepo = FakeDoorLockRepository()
            val viewModel = DoorLockViewModel(fakeRepo)
            WearApp(viewModel)
        }

        composeRule.onNodeWithText("Locked the Door").assertIsDisplayed()
    }

    @Test
    fun `Button displays Door is Unlocked when locked`() {
        composeRule.setContent {
            val fakeRepo = FakeDoorLockRepository()
            fakeRepo.setState(true)
            val viewModel = DoorLockViewModel(fakeRepo)
            WearApp(viewModel)
        }

        composeRule.onNodeWithText("Door is Unlocked").assertIsDisplayed()
    }

    @Test
    fun `clicking button when unlocked sets locked`() {
        composeRule.setContent {
            val fakeRepo = FakeDoorLockRepository()
            val viewModel = DoorLockViewModel(fakeRepo)
            WearApp(viewModel)
        }

        composeRule.onNodeWithText("Locked the Door").assertIsDisplayed()
        composeRule.onNodeWithText("Locked the Door").performClick()

        composeRule.onNodeWithText("Door is Locked").assertIsDisplayed()
        composeRule.onNodeWithText("Door is Unlocked").assertIsDisplayed()
    }

    @Test
    fun `clicking button when locked sets unlocked`() {
        composeRule.setContent {
            val fakeRepo = FakeDoorLockRepository()
            fakeRepo.setState(true)
            val viewModel = DoorLockViewModel(fakeRepo)
            WearApp(viewModel)
        }

        composeRule.onNodeWithText("Door is Locked").assertIsDisplayed()
        composeRule.onNodeWithText("Door is Unlocked").assertIsDisplayed()
        composeRule.onNodeWithText("Door is Unlocked").performClick()

        composeRule.onNodeWithText("Door is Unlocked").assertIsDisplayed()
        composeRule.onNodeWithText("Locked the Door").assertIsDisplayed()
    }

    private class FakeDoorLockRepository : dev.hubball.doorlockcheck.data.DoorLockRepository {
        private val _isLocked = MutableStateFlow(false)
        override val isLocked: StateFlow<Boolean> = _isLocked
        var lastToggled: Boolean? = null
        var lastSetLocked: Boolean? = null

        fun setState(value: Boolean) {
            _isLocked.value = value
        }

        override suspend fun setLocked(isLocked: Boolean) {
            lastSetLocked = isLocked
            _isLocked.value = isLocked
        }

        override suspend fun toggle() {
            lastToggled = !_isLocked.value
            _isLocked.value = !_isLocked.value
        }
    }
}

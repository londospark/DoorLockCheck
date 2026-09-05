package dev.hubball.doorlockcheck

import app.cash.turbine.test
import dev.hubball.doorlockcheck.data.DoorLockRepository
import dev.hubball.doorlockcheck.presentation.DoorLockViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DoorLockViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeRepository: FakeDoorLockRepository
    private lateinit var viewModel: DoorLockViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeDoorLockRepository()
        viewModel = DoorLockViewModel(fakeRepository)
    }

    @AfterEach
    fun cleanup() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is false when no data stored`() {
        assertFalse(fakeRepository.isLocked.value)
    }

    @Test
    fun `initial state is true when data stored`() {
        fakeRepository.setState(true)
        assertTrue(fakeRepository.isLocked.value)
    }

    @Test
    fun `toggleLockedStatus calls repository toggle`() = runTest {
        fakeRepository.setState(false)
        viewModel.toggleLockedStatus()
        assertTrue(fakeRepository.isLocked.value)
        assertEquals(true, fakeRepository.lastToggled)
    }

    @Test
    fun `toggleLockedStatus flips from locked to unlocked`() = runTest {
        fakeRepository.setState(true)
        viewModel.toggleLockedStatus()
        assertFalse(fakeRepository.isLocked.value)
    }

    @Test
    fun `setLockedStatus true updates state`() = runTest {
        viewModel.setLockedStatus(true)
        assertTrue(fakeRepository.isLocked.value)
        assertEquals(true, fakeRepository.lastSetLocked)
    }

    @Test
    fun `setLockedStatus false updates state`() = runTest {
        viewModel.setLockedStatus(false)
        assertFalse(fakeRepository.isLocked.value)
        assertEquals(false, fakeRepository.lastSetLocked)
    }

    @Test
    fun `viewModel isLocked flow mirrors repository flow`() = runTest {
        viewModel.isLocked.test {
            assertFalse(awaitItem())
            fakeRepository.setState(true)
            assertTrue(awaitItem())
            fakeRepository.setState(false)
            assertFalse(awaitItem())
        }
    }

    private class FakeDoorLockRepository : DoorLockRepository {
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
            lastToggled = _isLocked.value
            _isLocked.value = !_isLocked.value
        }
    }
}

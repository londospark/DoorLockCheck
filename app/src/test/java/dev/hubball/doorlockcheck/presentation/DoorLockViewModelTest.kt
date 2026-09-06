package dev.hubball.doorlockcheck.presentation

import app.cash.turbine.test
import dev.hubball.doorlockcheck.data.DoorLockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DoorLockViewModelTest {

    private lateinit var fakeRepository: FakeDoorLockRepository
    private lateinit var viewModel: DoorLockViewModel

    @Before
    fun setup() {
        fakeRepository = FakeDoorLockRepository()
        viewModel = DoorLockViewModel(fakeRepository)
    }

    @Test
    fun `isLocked exposes the current repository state`() {
        assertFalse(viewModel.isLocked.value)

        fakeRepository.setState(true)
        assertTrue(viewModel.isLocked.value)
    }

    @Test
    fun `toggleLockedStatus delegates to the repository`() {
        viewModel.toggleLockedStatus()
        assertTrue(fakeRepository.toggleCalls == 1)
        assertTrue(viewModel.isLocked.value)
    }

    @Test
    fun `toggle flips from locked back to unlocked`() {
        fakeRepository.setState(true)
        viewModel.toggleLockedStatus()
        assertFalse(viewModel.isLocked.value)
    }

    @Test
    fun `setLockedStatus true delegates to the repository`() {
        viewModel.setLockedStatus(true)
        assertEquals(true, fakeRepository.lastSetLocked)
        assertTrue(viewModel.isLocked.value)
    }

    @Test
    fun `setLockedStatus false delegates to the repository`() {
        viewModel.setLockedStatus(false)
        assertEquals(false, fakeRepository.lastSetLocked)
        assertFalse(viewModel.isLocked.value)
    }

    @Test
    fun `isLocked flow emits every state change`() = runTest {
        viewModel.isLocked.test {
            assertFalse(awaitItem())
            viewModel.setLockedStatus(true)
            assertTrue(awaitItem())
            viewModel.toggleLockedStatus()
            assertFalse(awaitItem())
            expectNoEvents()
        }
    }

    /** Hand-written fake: MockK is not needed for three methods, and fakes read better in failures. */
    private class FakeDoorLockRepository : DoorLockRepository {
        private val _isLocked = MutableStateFlow(false)
        override val isLocked: StateFlow<Boolean> = _isLocked
        var lastSetLocked: Boolean? = null
        var toggleCalls = 0

        fun setState(value: Boolean) {
            _isLocked.value = value
        }

        override fun setLocked(isLocked: Boolean) {
            lastSetLocked = isLocked
            _isLocked.value = isLocked
        }

        override fun toggle() {
            toggleCalls++
            setLocked(!_isLocked.value)
        }
    }
}

package com.elta.android.presentation.features.main.records.ui.compose

import com.elta.android.presentation.Events
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardSyncStateTest {
    @Test
    fun `failed device sync never advances last successful time and stays until dismissed`() = runTest {
        val state = DashboardSyncState("5 ч назад")
        state.handle(Events.Sync.Glucometer.Started, this)
        state.handle(Events.Sync.Glucometer.Error, this)
        advanceTimeBy(120_000)
        runCurrent()
        assertEquals("5 ч назад", state.displayedTime)
        assertTrue(state.isError)
        assertFalse(state.isSyncing)
        assertEquals(DashboardSyncTarget.METER, state.asUiState().retryTarget)
        assertNotNull(state.statusMessage)
        state.dismiss()
        assertNull(state.statusMessage)
    }

    @Test
    fun `server events stay invisible and never change device sync`() = runTest {
        val state = DashboardSyncState("5 ч назад")
        val events = listOf(Events.Sync.Server.Started, Events.Sync.Server.Success,
            Events.Sync.Server.Error, Events.Sync.Server.ErrorWithMessage)
        events.forEach { state.handle(it, this) }
        assertNull(state.statusMessage)
        assertFalse(state.isSyncing)
        state.handle(Events.Sync.Glucometer.Started, this)
        val busy = state.asUiState()
        events.forEach { state.handle(it, this); assertEquals(busy, state.asUiState()) }
        state.handle(Events.Sync.Glucometer.Error, this)
        val failed = state.asUiState()
        events.forEach { state.handle(it, this); assertEquals(failed, state.asUiState()) }
    }

    @Test
    fun `success notification clears without changing the device timestamp`() = runTest {
        val state = DashboardSyncState("5 ч назад")
        state.handle(Events.Sync.Glucometer.Success, this)
        advanceTimeBy(3_001)
        runCurrent()
        assertNull(state.statusMessage)
        assertEquals("5 ч назад", state.displayedTime)
    }

    @Test
    fun `old notification timeout cannot dismiss a newer failure`() = runTest {
        val state = DashboardSyncState("")
        state.handle(Events.Sync.Glucometer.Success, this)
        advanceTimeBy(1_000)
        state.handle(Events.Sync.Glucometer.Error, this)
        advanceTimeBy(5_000)
        runCurrent()
        assertTrue(state.isError)
        assertNotNull(state.statusMessage)
    }

    @Test
    fun `automatic device success does not remain busy waiting for a server sync`() = runTest {
        val state = DashboardSyncState("")
        state.handle(Events.Sync.Glucometer.Started, this)
        state.handle(Events.Sync.Glucometer.NoNewEvents, this)
        advanceTimeBy(60_001)
        runCurrent()
        assertFalse(state.isSyncing)
        assertFalse(state.isError)
        assertNull(state.statusMessage)
    }

    @Test
    fun `elapsed time handles unknown timestamps and clock moving backwards`() {
        assertNull(elapsedSyncMinutes(null, 600_000))
        assertNull(elapsedSyncMinutes(0, 600_000))
        assertEquals(0L, elapsedSyncMinutes(700_000, 600_000))
        assertEquals(5L, elapsedSyncMinutes(300_000, 600_000))
    }
}

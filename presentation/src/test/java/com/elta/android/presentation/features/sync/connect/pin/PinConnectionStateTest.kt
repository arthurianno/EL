package com.elta.android.presentation.features.sync.connect.pin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PinConnectionStateTest {
    @Test
    fun `PIN accepts exactly three ASCII digits and preserves leading zeroes`() {
        assertTrue("000".isGlucometerPinValid())
        assertTrue("037".isGlucometerPinValid())
        assertTrue("376".isGlucometerPinValid())
        listOf("", "37", "3760", " 376", "37a", "٣٧٦", "３７６").forEach {
            assertFalse(it.isGlucometerPinValid())
        }
        assertEquals("037", "0a3.7\n6".asGlucometerPin())
    }

    @Test
    fun `timeout is classified by operation rather than existing scan results`() {
        assertEquals(PinConnectionStage.NOT_FOUND, PinConnectionStage.SEARCH.operationError())
        assertEquals(PinConnectionStage.NOT_FOUND, PinConnectionStage.FOUND.operationError())
        assertEquals(PinConnectionStage.CONNECT_ERROR, PinConnectionStage.CONNECTING.operationError())
        assertEquals(PinConnectionStage.SYNC_ERROR, PinConnectionStage.CONNECTED.operationError())
    }

    @Test
    fun `action stays disabled during operations and until selection or complete PIN`() {
        listOf(PinConnectionStage.SEARCH, PinConnectionStage.CONNECTING, PinConnectionStage.CONNECTED).forEach {
            assertFalse(PinConnectionUiState(stage = it, canConnect = true, pin = "376").actionEnabled)
        }
        assertFalse(PinConnectionUiState(stage = PinConnectionStage.FOUND).actionEnabled)
        assertTrue(PinConnectionUiState(stage = PinConnectionStage.FOUND, canConnect = true).actionEnabled)
        assertFalse(PinConnectionUiState(stage = PinConnectionStage.ENTER_PIN, pin = "37").actionEnabled)
        assertTrue(PinConnectionUiState(stage = PinConnectionStage.ENTER_PIN, pin = "037").actionEnabled)
    }

    @Test
    fun `retry actions remain available after every operation error`() {
        listOf(PinConnectionStage.NOT_FOUND, PinConnectionStage.CONNECT_ERROR, PinConnectionStage.SYNC_ERROR).forEach {
            assertTrue(PinConnectionUiState(stage = it).actionEnabled)
        }
    }
}

package com.elta.android.presentation.features.version

import com.elta.android.presentation.features.version.optional.OptionalUpdatePromptState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OptionalUpdatePromptStateTest {
    @Test
    fun `waits for foreground startup and server version check`() {
        val state = readyState().apply { foreground = false; startupComplete = false; serverVersionChecked = false }
        assertFalse(state.requestPrompt(true, 0, NOW))
        state.foreground = true
        assertFalse(state.requestPrompt(true, 0, NOW))
        state.startupComplete = true
        assertFalse(state.requestPrompt(true, 0, NOW))
        state.serverVersionChecked = true
        assertTrue(state.requestPrompt(true, 0, NOW))
    }

    @Test
    fun `waits until all concurrent sync operations finish`() {
        val state = readyState().apply { glucometerSync = true; backendSync = true; serverSync = true }
        assertFalse(state.requestPrompt(true, 0, NOW))
        state.glucometerSync = false
        assertFalse(state.requestPrompt(true, 0, NOW))
        state.serverSync = false
        assertFalse(state.requestPrompt(true, 0, NOW))
        state.backendSync = false
        assertTrue(state.requestPrompt(true, 0, NOW))
    }

    @Test
    fun `mandatory update offline state and missing updates prevent prompts`() {
        assertFalse(readyState().apply { mandatoryUpdate = true }.requestPrompt(true, 0, NOW))
        assertFalse(readyState().apply { online = false }.requestPrompt(true, 0, NOW))
        assertFalse(readyState().requestPrompt(false, 0, NOW))
    }

    @Test
    fun `mandatory update arriving later still takes priority after the optional dialog closes`() {
        val state = readyState()
        assertTrue(state.requestPrompt(true, 0, NOW))
        state.onDialogVisibilityChanged(true)
        state.mandatoryUpdate = true
        state.onDialogVisibilityChanged(false)
        assertFalse(state.requestPrompt(true, 0, NOW))
    }

    @Test
    fun `three day interval is measured from the actual display`() {
        val state = readyState()
        val interval = OptionalUpdatePromptState.PROMPT_INTERVAL_MILLIS
        assertFalse(state.requestPrompt(true, NOW - interval + 1, NOW))
        assertTrue(state.requestPrompt(true, NOW - interval, NOW))
    }

    @Test
    fun `does not duplicate requested or visible dialogs`() {
        val state = readyState()
        assertTrue(state.requestPrompt(true, 0, NOW))
        assertFalse(state.requestPrompt(true, 0, NOW))
        state.onDialogVisibilityChanged(true)
        assertFalse(state.requestPrompt(true, 0, NOW))
        state.onDialogVisibilityChanged(false)
        assertFalse(state.requestPrompt(true, NOW, NOW + 1000))
    }

    @Test
    fun `an undisplayed request can be retried without consuming the interval`() {
        val state = readyState()
        assertTrue(state.requestPrompt(true, 0, NOW))
        state.onDialogVisibilityChanged(false)
        assertTrue(state.requestPrompt(true, 0, NOW))
    }

    @Test
    fun `a backwards clock change does not block prompts forever`() {
        assertTrue(readyState().requestPrompt(true, NOW + 1000, NOW))
    }

    private fun readyState() = OptionalUpdatePromptState().apply {
        foreground = true
        online = true
        startupComplete = true
        serverVersionChecked = true
    }

    private companion object {
        const val NOW = 1_800_000_000_000L
    }
}

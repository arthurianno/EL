package com.elta.android.presentation.features.consultant.viewmodel

import androidx.lifecycle.Lifecycle
import org.junit.Assert.assertEquals
import org.junit.Test

class ConsultantScreenPresenceTest {
    @Test
    fun `notifications resume when the chat loses focus`() {
        val states = mutableListOf<Boolean>()
        val presence = ConsultantScreenPresence { states += it }

        presence.onEvent(Lifecycle.Event.ON_CREATE)
        presence.onEvent(Lifecycle.Event.ON_RESUME)
        presence.onEvent(Lifecycle.Event.ON_PAUSE)
        presence.onEvent(Lifecycle.Event.ON_RESUME)
        presence.onEvent(Lifecycle.Event.ON_DESTROY)

        assertEquals(listOf(true, false, true, false), states)
    }

    @Test
    fun `leaving the chat immediately re-enables notifications`() {
        var active = false
        val presence = ConsultantScreenPresence { active = it }

        presence.onEvent(Lifecycle.Event.ON_RESUME)
        presence.leave()

        assertEquals(false, active)
    }
}

package com.elta.android.presentation.features.consultant.viewmodel

import androidx.lifecycle.Lifecycle
import com.elta.android.domain.features.consultant.model.WebimUser
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConsultantSessionLifecycleTest {
    private val user = WebimUser(id = "user", name = "Test")

    @Test
    fun `resumes once after session creation`() = runTest {
        val events = mutableListOf<String>()
        val finishCreation = CompletableDeferred<Unit>()
        val lifecycle = ConsultantSessionLifecycle(
            createSession = {
                events += "create"
                finishCreation.await()
            },
            resumeSession = { events += "resume" },
            pauseSession = {},
            destroySession = {}
        )

        lifecycle.onEvent(Lifecycle.Event.ON_CREATE, this, user)
        lifecycle.onEvent(Lifecycle.Event.ON_START, this, user)
        lifecycle.onEvent(Lifecycle.Event.ON_RESUME, this, user)
        runCurrent()
        assertEquals(listOf("create"), events)

        finishCreation.complete(Unit)
        advanceUntilIdle()
        assertEquals(listOf("create", "resume"), events)
    }

    @Test
    fun `does not resume after screen pauses while session is being created`() = runTest {
        val events = mutableListOf<String>()
        val finishCreation = CompletableDeferred<Unit>()
        val lifecycle = ConsultantSessionLifecycle(
            createSession = {
                events += "create"
                finishCreation.await()
            },
            resumeSession = { events += "resume" },
            pauseSession = { events += "pause" },
            destroySession = {}
        )

        lifecycle.onEvent(Lifecycle.Event.ON_CREATE, this, user)
        lifecycle.onEvent(Lifecycle.Event.ON_START, this, user)
        runCurrent()
        lifecycle.onEvent(Lifecycle.Event.ON_PAUSE, this, user)
        finishCreation.complete(Unit)
        advanceUntilIdle()

        assertEquals(listOf("create", "pause"), events)
    }

    @Test
    fun `recreates a missing session before retrying resume`() = runTest {
        val events = mutableListOf<String>()
        var resumeAttempts = 0
        val lifecycle = ConsultantSessionLifecycle(
            createSession = { events += "create" },
            resumeSession = {
                events += "resume"
                if (++resumeAttempts == 1) throw IllegalStateException("Session is missing")
            },
            pauseSession = {},
            destroySession = {}
        )

        lifecycle.onEvent(Lifecycle.Event.ON_CREATE, this, user)
        lifecycle.onEvent(Lifecycle.Event.ON_START, this, user)
        advanceUntilIdle()

        assertEquals(listOf("create", "resume", "create", "resume"), events)
    }

    @Test
    fun `destroy cancels pending session creation`() = runTest {
        val finishCreation = CompletableDeferred<Unit>()
        var created = 0
        var destroyed = 0
        val lifecycle = ConsultantSessionLifecycle(
            createSession = {
                finishCreation.await()
                created++
            },
            resumeSession = {},
            pauseSession = {},
            destroySession = { destroyed++ }
        )

        lifecycle.onEvent(Lifecycle.Event.ON_CREATE, this, user)
        runCurrent()
        lifecycle.onEvent(Lifecycle.Event.ON_DESTROY, this, user)
        finishCreation.complete(Unit)
        advanceUntilIdle()

        assertEquals(0, created)
        assertEquals(1, destroyed)
    }
}

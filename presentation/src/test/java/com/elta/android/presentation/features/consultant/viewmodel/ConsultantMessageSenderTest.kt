package com.elta.android.presentation.features.consultant.viewmodel

import com.elta.android.domain.features.consultant.usecase.SendMessageUseCase
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConsultantMessageSenderTest {
    @Test
    fun `ignores duplicate request until the first one completes`() = runTest {
        val release = CompletableDeferred<Unit>()
        val params = SendMessageUseCase.Params(text = "hello")
        var attempts = 0
        var accepted = 0
        val sender = ConsultantMessageSender {
            attempts++
            release.await()
        }

        assertTrue(sender.send(this, params, onAccepted = { accepted++ }, onFailure = {}))
        assertFalse(sender.send(this, params, onAccepted = { accepted++ }, onFailure = {}))
        runCurrent()
        assertEquals(1, attempts)

        release.complete(Unit)
        advanceUntilIdle()
        assertEquals(1, accepted)

        assertTrue(sender.send(this, params, onAccepted = { accepted++ }, onFailure = {}))
        advanceUntilIdle()
        assertEquals(2, attempts)
        assertEquals(2, accepted)
    }

    @Test
    fun `reports a failed request and allows retry`() = runTest {
        val params = SendMessageUseCase.Params(text = "hello")
        var shouldFail = true
        var accepted = 0
        val errors = mutableListOf<Exception>()
        val sender = ConsultantMessageSender {
            if (shouldFail) throw IOException("Offline")
        }

        sender.send(this, params, onAccepted = { accepted++ }, onFailure = errors::add)
        advanceUntilIdle()
        assertEquals(0, accepted)
        assertEquals(1, errors.size)

        shouldFail = false
        assertTrue(sender.send(this, params, onAccepted = { accepted++ }, onFailure = errors::add))
        advanceUntilIdle()
        assertEquals(1, accepted)
    }
}

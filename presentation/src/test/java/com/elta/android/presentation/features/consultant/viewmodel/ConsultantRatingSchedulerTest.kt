package com.elta.android.presentation.features.consultant.viewmodel

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConsultantRatingSchedulerTest {
    @Test
    fun `timer sends the latest selected rating once`() = runTest {
        val sent = mutableListOf<Int>()
        var stars = 2
        var finished = 0
        val scheduler = ConsultantRatingScheduler { sent += it }

        scheduler.schedule(this, { stars }) { finished++ }
        runCurrent()
        stars = 5
        scheduler.schedule(this, { stars }) { finished++ }
        advanceTimeBy(10_000)
        runCurrent()

        assertEquals(listOf(5), sent)
        assertEquals(1, finished)
    }

    @Test
    fun `stopping before timer sends rating and cancels delayed send`() = runTest {
        val sent = mutableListOf<Int>()
        var finished = 0
        val scheduler = ConsultantRatingScheduler { sent += it }

        scheduler.schedule(this, { 4 }) { finished++ }
        runCurrent()
        advanceTimeBy(5_000)
        scheduler.flush({ 4 }) { finished++ }
        advanceUntilIdle()

        assertEquals(listOf(4), sent)
        assertEquals(1, finished)
    }

    @Test
    fun `stopping during an active send waits without sending twice`() = runTest {
        val releaseSend = CompletableDeferred<Unit>()
        val sent = mutableListOf<Int>()
        var finished = 0
        val scheduler = ConsultantRatingScheduler {
            sent += it
            releaseSend.await()
        }

        scheduler.schedule(this, { 3 }) { finished++ }
        runCurrent()
        advanceTimeBy(10_000)
        runCurrent()
        val flush = launch { scheduler.flush({ 3 }) { finished++ } }
        runCurrent()
        assertEquals(listOf(3), sent)

        releaseSend.complete(Unit)
        flush.join()
        assertEquals(1, finished)
    }
}

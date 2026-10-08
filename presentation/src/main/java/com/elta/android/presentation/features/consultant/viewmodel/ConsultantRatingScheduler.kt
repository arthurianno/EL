package com.elta.android.presentation.features.consultant.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Schedules one rating send and flushes it when the screen stops. Called from the main thread. */
internal class ConsultantRatingScheduler(
    private val sendRating: suspend (Int) -> Unit
) {
    private var pendingJob: Job? = null
    private var waitingForDelay = false

    fun schedule(
        scope: CoroutineScope,
        currentStars: () -> Int?,
        onFinished: () -> Unit
    ) {
        if (pendingJob?.isActive == true) return
        pendingJob?.cancel()
        waitingForDelay = true
        pendingJob = scope.launch {
            delay(RATING_DELAY_MILLIS)
            waitingForDelay = false
            sendCurrentRating(currentStars, onFinished)
        }
    }

    suspend fun flush(currentStars: () -> Int?, onFinished: () -> Unit) {
        val job = pendingJob ?: return
        pendingJob = null
        if (!job.isActive) return

        if (waitingForDelay) {
            waitingForDelay = false
            job.cancelAndJoin()
            sendCurrentRating(currentStars, onFinished)
        } else {
            job.join()
        }
    }

    private suspend fun sendCurrentRating(currentStars: () -> Int?, onFinished: () -> Unit) {
        currentStars()?.let { sendRating(it) }
        onFinished()
    }
}

private const val RATING_DELAY_MILLIS = 10_000L

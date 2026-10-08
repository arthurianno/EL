package com.elta.android.presentation.features.consultant.viewmodel

import android.net.Uri
import com.elta.android.domain.features.consultant.usecase.DownloadFileUseCase
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import timber.log.Timber

/** Tracks downloads by message so cancelling one file cannot cancel another. */
class ConsultantFileDownloads @Inject constructor(
    private val downloadFile: DownloadFileUseCase
) {
    private val jobs = mutableMapOf<String, Job>()

    fun start(
        scope: CoroutineScope,
        messageId: String,
        url: String,
        fileName: String,
        onSuccess: (Uri) -> Unit,
        onFailure: () -> Unit
    ) {
        if (jobs[messageId]?.isActive == true) return
        val job = scope.launch(start = CoroutineStart.LAZY) {
            try {
                val uri = downloadFile(url, fileName)
                currentCoroutineContext().ensureActive()
                onSuccess(uri)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Timber.e(error, "Failed to download consultant file")
                onFailure()
            } finally {
                if (jobs[messageId] === currentCoroutineContext()[Job]) jobs.remove(messageId)
            }
        }
        jobs[messageId] = job
        job.start()
    }

    fun cancel(messageId: String) {
        jobs.remove(messageId)?.cancel()
    }
}

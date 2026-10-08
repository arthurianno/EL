package com.elta.android.presentation.features.consultant.viewmodel

import android.net.Uri
import com.elta.android.domain.common.usecase.FileDeleteUseCase
import com.elta.android.presentation.features.consultant.model.CachingState
import com.elta.android.presentation.features.consultant.model.MessageType
import com.elta.android.presentation.features.consultant.model.MessageUiEntity
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope

class ConsultantFileController @Inject constructor(
    private val downloads: ConsultantFileDownloads,
    private val deleteFile: FileDeleteUseCase
) {
    fun handle(
        scope: CoroutineScope,
        message: MessageUiEntity,
        openFile: (Uri, MessageType) -> Unit,
        updateCache: (String, CachingState, Uri?) -> Unit
    ) {
        val document = message.document ?: return

        when (document.cachingState) {
            CachingState.Cached -> {
                val uri = document.uri
                if (uri != null) {
                    openFile(uri, message.type)
                } else {
                    startDownload(scope, message, updateCache)
                }
            }

            CachingState.Downloading -> {
                downloads.cancel(message.id)
                document.uri?.let(deleteFile::invoke)
                updateCache(message.id, CachingState.NotCached, null)
            }

            CachingState.NotCached -> startDownload(scope, message, updateCache)
        }
    }

    private fun startDownload(
        scope: CoroutineScope,
        message: MessageUiEntity,
        updateCache: (String, CachingState, Uri?) -> Unit
    ) {
        val document = message.document ?: return
        updateCache(message.id, CachingState.Downloading, null)
        downloads.start(
            scope = scope,
            messageId = message.id,
            url = document.url.orEmpty(),
            fileName = document.fileName,
            onSuccess = { uri -> updateCache(message.id, CachingState.Cached, uri) },
            onFailure = { updateCache(message.id, CachingState.NotCached, null) }
        )
    }
}

package com.elta.android.domain.features.consultant.usecase

import android.net.Uri
import com.elta.android.common.utils.timestamp
import com.elta.android.domain.common.addExtension
import com.elta.android.domain.common.model.FileDirectory
import com.elta.android.domain.common.model.FileInfo
import com.elta.android.domain.common.model.FileType
import com.elta.android.domain.common.repository.FileInfoRepository
import com.elta.android.domain.common.repository.MediaRepository
import com.elta.android.domain.features.consultant.model.ContentType
import com.elta.android.domain.features.consultant.model.ContentType.Companion.toContentType
import com.elta.android.domain.features.consultant.repository.ConsultantRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val consultantRepository: ConsultantRepository,
    private val mediaRepository: MediaRepository,
    private val fileInfoRepository: FileInfoRepository
) {
    suspend operator fun invoke(params: Params) {
        if (!params.text.isNullOrEmpty()) consultantRepository.sendMessage(message = params.text)
        else {
            val fileType = mediaRepository.getFileType(params.uri ?: Uri.EMPTY) ?: FileType.Pdf
            val fileName = params.uri?.lastPathSegment.orEmpty()
                .replace(":", "_")

            when (fileType) {
                FileType.Voice,
                FileType.Jpg,
                FileType.Heif,
                FileType.Png -> sendFile(
                    params = params,
                    type = fileType,
                    fileName = fileName
                )

                else -> params.uri?.let {
                    sendSharedFile(
                        fileUri = it,
                        type = fileType,
                        fileName = fileName.addExtension(fileType)
                    )
                }
            }
        }
    }

    private suspend fun sendSharedFile(fileUri: Uri, type: FileType, fileName: String) {
        val cachedFileUri = mediaRepository.cacheFileInDirectory(
            directory = FileDirectory.Document,
            fileName = fileName,
            uri = fileUri
        )

        val cachedFile = mediaRepository.getFileFromDirectory(
            directory = FileDirectory.Document,
            fileName = cachedFileUri.lastPathSegment.orEmpty()
        )
        saveFileInfo(fileName, type.toContentType())
        consultantRepository.sendFile(cachedFile)
    }

    private suspend fun sendFile(params: Params, type: FileType, fileName: String) {
        val fileUri = if (params.needCaching) {
            val uriFromCache = mediaRepository.cachedFile(
                fileName = fileName,
                fileType = type,
                sourceUri = params.uri ?: Uri.EMPTY
            )
            saveFileInfo(fileName, type.toContentType())

            uriFromCache
        } else params.uri

        val cachedFile = mediaRepository.getCachedFile(fileUri?.lastPathSegment.orEmpty())
        consultantRepository.sendFile(cachedFile)
    }

    private fun saveFileInfo(fileName: String, contentType: ContentType) {
        fileInfoRepository.update(
            listOf(
                FileInfo(
                    id = fileName.hashCode().toLong(),
                    name = fileName,
                    timestamp = timestamp(),
                    type = contentType
                )
            )
        )
    }

    data class Params(
        val text: String? = null,
        val uri: Uri? = null,
        val needCaching: Boolean = false
    )
}

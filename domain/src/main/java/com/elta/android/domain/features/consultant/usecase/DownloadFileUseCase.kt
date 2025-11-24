package com.elta.android.domain.features.consultant.usecase

import android.net.Uri
import com.elta.android.common.utils.timestamp
import com.elta.android.domain.common.model.FileDirectory
import com.elta.android.domain.common.model.FileInfo
import com.elta.android.domain.common.repository.DownloadRepository
import com.elta.android.domain.common.repository.FileInfoRepository
import com.elta.android.domain.common.repository.MediaRepository
import com.elta.android.domain.features.consultant.model.ContentType.Companion.toContentType
import javax.inject.Inject

class DownloadFileUseCase @Inject constructor(
    private val downloadRepository: DownloadRepository,
    private val mediaRepository: MediaRepository,
    private val fileInfoRepository: FileInfoRepository
) {

    suspend operator fun invoke(url: String, fileName: String): Uri {
        val downloadedFile = downloadRepository.downloadFile(
            url = url,
            destinationDirectory = FileDirectory.Document,
            destinationFile = fileName
        )

        val fileUri = mediaRepository.getFileUri(downloadedFile)
        val contentType = mediaRepository.getFileType(fileUri)?.toContentType()

        contentType?.let { type ->
            val fileInformation = FileInfo(
                id = fileName.hashCode().toLong(),
                name = fileName,
                timestamp = timestamp(),
                type = type
            )

            val list = listOf(fileInformation)

            fileInfoRepository.update(list)
        }

        return fileUri
    }
}

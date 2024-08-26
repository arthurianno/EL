package com.elta.android.domain.features.consultant.usecase

import android.net.Uri
import com.elta.android.domain.common.model.FileDirectory
import com.elta.android.domain.common.repository.MediaRepository
import com.elta.android.domain.features.consultant.model.ContentType
import javax.inject.Inject

class GetCachedFilesUriUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    operator fun invoke(fileName: String, type: ContentType): Uri? {
        val fileUri = when (type) {
            ContentType.Image,
            ContentType.Voice -> checkFromCache(fileName)

            ContentType.DocumentPdf,
            ContentType.Video -> checkFromDirectory(fileName)

            else -> null
        }

        return fileUri
    }

    private fun checkFromCache(fileName: String): Uri? {
        val file = mediaRepository.getCachedFile(
            fileName = fileName
        )
        return try {
            file?.let { mediaRepository.getFileUri(file) }
        } catch (e: Exception) {
            null
        }
    }

    private fun checkFromDirectory(fileName: String): Uri? {
        val file = mediaRepository.getFileFromDirectory(
            directory = FileDirectory.Document,
            fileName = fileName
        )

        return try {
            file?.let { mediaRepository.getFileUri(it) }
        } catch (e: Exception) {
            null
        }
    }
}

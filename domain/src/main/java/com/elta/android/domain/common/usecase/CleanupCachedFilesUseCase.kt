package com.elta.android.domain.common.usecase

import android.net.Uri
import com.elta.android.common.utils.SECOND_IN_DAY
import com.elta.android.common.utils.timestamp
import com.elta.android.domain.common.addExtension
import com.elta.android.domain.common.model.FileDirectory
import com.elta.android.domain.common.repository.FileInfoRepository
import com.elta.android.domain.common.repository.MediaRepository
import com.elta.android.domain.features.consultant.model.ContentType
import com.elta.android.domain.features.consultant.model.ContentType.Companion.toFileType
import javax.inject.Inject
import kotlin.math.abs

class CleanupCachedFilesUseCase @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val fileInfoRepository: FileInfoRepository
) {
    operator fun invoke() {
        val savedFilesInfo = fileInfoRepository.getAll()
            .filter { abs(it.timestamp - timestamp()) > SECOND_IN_DAY * 7 }

        savedFilesInfo
            .takeIf { it.isNotEmpty() }
            ?.filter {
                it.type == ContentType.DocumentPdf ||
                        it.type == ContentType.Video
            }
            ?.forEach {
                val file = mediaRepository.getFileFromDirectory(FileDirectory.Document, it.name)
                val uri = Uri.fromFile(file)
                mediaRepository.deleteFile(uri)
            }

        savedFilesInfo
            .takeIf { it.isNotEmpty() }
            ?.filter {
                it.type == ContentType.Image ||
                        it.type == ContentType.Voice
            }?.forEach { info ->
                val file =
                    mediaRepository.getCachedFile(info.name addExtension info.type.toFileType())
                val uri = Uri.fromFile(file)
                mediaRepository.deleteFile(uri)
            }


        val idsDeletedFiles = savedFilesInfo.map { it.id }

        fileInfoRepository.clear(idsDeletedFiles)
    }
}

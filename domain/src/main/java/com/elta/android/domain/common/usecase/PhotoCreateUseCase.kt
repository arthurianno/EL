package com.elta.android.domain.common.usecase

import android.net.Uri
import com.elta.android.common.utils.timestamp
import com.elta.android.domain.common.getFileName
import com.elta.android.domain.common.repository.FileInfoRepository
import com.elta.android.domain.common.repository.MediaRepository
import com.elta.android.domain.features.consultant.model.ContentType
import com.elta.android.domain.common.model.FileInfo
import javax.inject.Inject

class PhotoCreateUseCase @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val fileInfoRepository: FileInfoRepository
) {
    operator fun invoke(): Uri {
        val fileUri = mediaRepository.createPhoto()
        val fileName = fileUri.getFileName()
        fileInfoRepository.update(
            listOf(
                FileInfo(
                    id = fileName.hashCode().toLong(),
                    name = fileName.orEmpty(),
                    timestamp = timestamp(),
                    type = ContentType.Image
                )
            )
        )
        return fileUri
    }
}

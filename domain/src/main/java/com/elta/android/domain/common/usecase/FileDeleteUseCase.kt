package com.elta.android.domain.common.usecase

import android.net.Uri
import com.elta.android.domain.common.repository.FileInfoRepository
import com.elta.android.domain.common.repository.MediaRepository
import javax.inject.Inject

class FileDeleteUseCase @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val fileInfoRepository: FileInfoRepository
) {
    operator fun invoke(uri: Uri) {
        mediaRepository.deleteFile(uri)
        val deletedFileId = uri.lastPathSegment.hashCode().toLong()
        fileInfoRepository.clear(listOf(deletedFileId))
    }
}

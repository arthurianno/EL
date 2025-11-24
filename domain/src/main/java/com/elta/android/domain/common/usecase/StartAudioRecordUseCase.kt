package com.elta.android.domain.common.usecase

import android.net.Uri
import com.elta.android.common.utils.timestamp
import com.elta.android.domain.common.getFileName
import com.elta.android.domain.common.model.FileInfo
import com.elta.android.domain.common.repository.FileInfoRepository
import com.elta.android.domain.common.repository.MediaRepository
import com.elta.android.domain.features.consultant.model.ContentType
import com.elta.android.domain.features.consultant.repository.AudioRecorderRepository
import java.io.File
import javax.inject.Inject

class StartAudioRecordUseCase @Inject constructor(
    private val audioRepository: AudioRecorderRepository,
    private val mediaRepository: MediaRepository,
    private val fileInfoRepository: FileInfoRepository
) {
    operator fun invoke(): Uri {
        val fileForWriting = mediaRepository.createAudioFile()
        saveFileInfo(fileForWriting)
        audioRepository.start(fileForWriting)
        return Uri.fromFile(fileForWriting)
    }

    private fun saveFileInfo(fileForWriting: File) {
        val fileUri = Uri.fromFile(fileForWriting)
        val fileName = fileUri.getFileName().orEmpty()
        val fileInformation = FileInfo(
            id = fileName.hashCode().toLong(),
            name = fileName,
            timestamp = timestamp(),
            type = ContentType.Voice
        )
        fileInfoRepository.update(listOf(fileInformation))
    }
}

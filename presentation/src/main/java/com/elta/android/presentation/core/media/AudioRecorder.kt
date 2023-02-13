package com.elta.android.presentation.core.media

import kotlinx.coroutines.flow.Flow
import java.io.File

interface AudioRecorder {
    val audioFile: File?
    val volumeFlow: Flow<Float>
    val volumeRecordDelay: Long
    fun clearAudioFile()
    suspend fun deleteAudioFile()
    fun recordStop(release: Boolean = false)
    fun startRecord()
}
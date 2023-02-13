package com.elta.android.presentation.core.media

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface AudioRecorder {
    val volumeFlow: Flow<Float>
    val volumeRecordDelay: Long
    suspend fun deleteAudioFile()
    fun recordStop(release: Boolean = false): Uri?
    fun recordStart()
}
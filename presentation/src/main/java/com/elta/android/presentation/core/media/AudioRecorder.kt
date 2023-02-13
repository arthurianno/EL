package com.elta.android.presentation.core.media

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface AudioRecorder {
    val volumeFlow: Flow<Float>
    suspend fun deleteFile()
    fun stop(release: Boolean = false): Uri?
    fun start(stepMillis: Long)
}
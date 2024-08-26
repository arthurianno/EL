package com.elta.android.domain.features.consultant.repository

import kotlinx.coroutines.flow.Flow
import java.io.File

interface AudioRecorderRepository {
    fun start(writingFile: File)
    fun stop()
    val volume: Flow<Float>
}

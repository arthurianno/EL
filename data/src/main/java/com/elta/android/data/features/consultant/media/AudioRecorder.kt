package com.elta.android.data.features.consultant.media

import java.io.File

interface AudioRecorder {
    fun start(writingFile: File)
    fun stop(release: Boolean = false)
    fun getAmplitude(): Int
}

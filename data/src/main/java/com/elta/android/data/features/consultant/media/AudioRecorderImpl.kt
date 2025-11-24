package com.elta.android.data.features.consultant.media

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import javax.inject.Inject

internal const val MAX_FILE_SIZE = 10000000L
internal const val SAMPLING_RATE = 44100
internal const val AUDIO_BITRATE = 384000
internal const val AUDIO_CHANNELS_COUNT = 2

class AudioRecorderImpl @Inject constructor(
    context: Context
) : AudioRecorder {
    private val mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MediaRecorder(context)
    } else {
        MediaRecorder()
    }

    override fun start(writingFile: File) {
        with(mediaRecorder) {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioChannels(AUDIO_CHANNELS_COUNT)
            setAudioSamplingRate(SAMPLING_RATE)
            setAudioEncodingBitRate(AUDIO_BITRATE)
            setMaxFileSize(MAX_FILE_SIZE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) setOutputFile(writingFile)
            else setOutputFile(writingFile.path)
            prepare()
            start()
        }
    }

    override fun stop(release: Boolean) {
        with(mediaRecorder) {
            stop()
        }
    }

    override fun getAmplitude(): Int = mediaRecorder.maxAmplitude
}

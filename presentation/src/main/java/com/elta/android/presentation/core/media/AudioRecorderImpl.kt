package com.elta.android.presentation.core.media

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.core.net.toUri
import com.elta.android.domain.common.usecase.AudioRecordCreateUseCase
import com.elta.android.domain.common.usecase.FileDeleteUseCase
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import java.io.File
import javax.inject.Inject

internal const val MAX_FILE_SIZE = 10000000L
internal const val SAMPLING_RATE = 44100
internal const val AUDIO_BITRATE = 960
internal const val AUDIO_CHANNELS_COUNT = 1
internal const val VOLUME_MAX_LEVEL = 7500F

class AudioRecorderImpl @Inject constructor(
    private val audioFileCreate: AudioRecordCreateUseCase,
    private val fileDelete: FileDeleteUseCase,
    context: Context
) : AudioRecorder {
    private val mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MediaRecorder(context)
    } else {
        MediaRecorder()
    }

    private var volumeLevelTicker: ReceiveChannel<Unit>? = null
    override val volumeFlow: Flow<Float>
        get() = volumeLevelTicker
            ?.receiveAsFlow()
            ?.map { mediaRecorder.maxAmplitude / VOLUME_MAX_LEVEL } ?: emptyFlow()

    private var audioFile: File? = null

    override suspend fun deleteFile() {
        audioFile?.toUri()?.let { fileDelete(it) }
        audioFile = null
    }

    override fun stop(release: Boolean): Uri? {
        with(mediaRecorder) {
            stop()
            if (release) {
                release()
            } else {
                reset()
            }
        }
        volumeLevelTicker?.cancel()
        volumeLevelTicker = null
        return audioFile?.toUri()
    }

    @OptIn(ObsoleteCoroutinesApi::class)
    override fun start(stepMillis: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            runCatching {
                volumeLevelTicker = ticker(
                    delayMillis = stepMillis,
                    initialDelayMillis = stepMillis
                )
                audioFile = audioFileCreate()
                with(mediaRecorder) {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioChannels(AUDIO_CHANNELS_COUNT)
                    setAudioSamplingRate(SAMPLING_RATE)
                    setAudioEncodingBitRate(AUDIO_BITRATE)
                    setMaxFileSize(MAX_FILE_SIZE)
                    setOutputFile(audioFile)
                    prepare()
                    start()
                }
            }
                .onFailure {
                    volumeLevelTicker = null
                }
        }
    }
}


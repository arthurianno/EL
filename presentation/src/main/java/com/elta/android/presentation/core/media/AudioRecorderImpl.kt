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

private const val MAX_FILE_SIZE = 10000000L
private const val VOLUME_TIMER_DELAY = 100L
private const val VOLUME_MAX_LEVEL = 7500F

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

    override val volumeRecordDelay: Long
        get() = VOLUME_TIMER_DELAY

    private var audioFile: File? = null

    override suspend fun deleteAudioFile() {
        audioFile?.toUri()?.let { fileDelete(it) }
        audioFile = null
    }

    override fun recordStop(release: Boolean): Uri? {
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
    override fun recordStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            volumeLevelTicker = ticker(
                delayMillis = VOLUME_TIMER_DELAY,
                initialDelayMillis = VOLUME_TIMER_DELAY
            )
            audioFile = audioFileCreate()
            with(mediaRecorder) {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setMaxFileSize(MAX_FILE_SIZE)
                setOutputFile(audioFile)
                prepare()
                start()
            }
        }
    }
}


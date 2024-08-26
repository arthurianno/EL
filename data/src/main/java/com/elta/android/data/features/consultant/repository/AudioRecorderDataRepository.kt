package com.elta.android.data.features.consultant.repository

import com.elta.android.data.features.consultant.media.AudioRecorder
import com.elta.android.domain.features.consultant.repository.AudioRecorderRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import javax.inject.Inject

class AudioRecorderDataRepository @Inject constructor(
    private val audioRecorder: AudioRecorder,
    dispatcher: CoroutineDispatcher
) : AudioRecorderRepository {

    @OptIn(ObsoleteCoroutinesApi::class)
    private val tickerChannel = ticker(
        delayMillis = 100L,
        initialDelayMillis = 500L
    )

    override val volume: Flow<Float> = flow {
        for (tick in tickerChannel) {
            val amplitude = audioRecorder.getAmplitude() / VOLUME_MAX_LEVEL
            emit(amplitude)
        }
    }.flowOn(dispatcher)

    override fun start(writingFile: File) {
        audioRecorder.start(writingFile)
    }

    override fun stop() {
        audioRecorder.stop()
    }
}

private const val VOLUME_MAX_LEVEL = 7500F

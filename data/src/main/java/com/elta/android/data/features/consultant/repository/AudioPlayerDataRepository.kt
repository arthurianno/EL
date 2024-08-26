package com.elta.android.data.features.consultant.repository

import android.net.Uri
import com.elta.android.data.features.consultant.media.AudioPlayer
import com.elta.android.domain.features.consultant.repository.AudioPlayerRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class AudioPlayerDataRepository @Inject constructor(
    private val audioPlayer: AudioPlayer,
    dispatcher: CoroutineDispatcher
) : AudioPlayerRepository {

    @OptIn(ObsoleteCoroutinesApi::class)
    private val tickerChannel = ticker(delayMillis = 1000L)

    override val trackPosition: Flow<Int> =
        flow {
            for (tick in tickerChannel) {
                val trackPosition = audioPlayer.getPlaybackTime()
                emit(trackPosition)
            }
        }.flowOn(dispatcher)

    override fun start(uri: Uri, trackPosition: Int?) {
        if (trackPosition == null) {
            audioPlayer.prepare(uri)
        }
        audioPlayer.play(trackPosition)
    }

    override fun stop(time: Long) {
        audioPlayer.stop()
    }

    override fun pause(): Int = audioPlayer.pause()
}
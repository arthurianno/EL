package com.elta.android.presentation.core.media

import android.content.Context
import android.media.AudioTrack
import android.media.MediaPlayer
import android.net.Uri
import com.elta.android.domain.common.usecase.CachedSoundUseCase
import javax.inject.Inject

private const val PLAY_STEP_MILLIS = 100
private const val START_POSITION = 0
private const val SAMPLE_RATE = 44100

class AudioPlayerImpl @Inject constructor(
    private val cachedSound: CachedSoundUseCase,
    private val context: Context
) : AudioPlayer {
    private val player: MediaPlayer = MediaPlayer()
    private var audioTrack: AudioTrack? = null
    var currentPosition: Int = START_POSITION
        private set


    override val volumeList: List<Float> = emptyList()

    override fun prepare(uri: Uri) {
        with(player) {
            setDataSource(context, uri)
            prepare()

        }
    }

    override fun play(position: Int?) {
        position?.let { player.seekTo(it * PLAY_STEP_MILLIS) }
            ?: player.start()
    }

    override fun stop() {
        player.stop()
        currentPosition = START_POSITION
    }

    override fun pause(): Int {
        player.pause()
        return player.currentPosition.also {
            currentPosition = it
        }
    }
}
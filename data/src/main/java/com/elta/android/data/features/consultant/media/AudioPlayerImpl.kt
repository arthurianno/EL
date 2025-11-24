package com.elta.android.data.features.consultant.media

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import javax.inject.Inject

private const val MILLS_BEFORE_SEEK_TO = 400

class AudioPlayerImpl @Inject constructor(
    private val context: Context
) : AudioPlayer {
    private val player: MediaPlayer = MediaPlayer()

    override fun getPlaybackTime(): Int = player.currentPosition

    override fun prepare(uri: Uri) {
        with(player) {
            reset()
            setDataSource(context, uri)
            prepare()
        }
    }

    override fun play(position: Int?) {
        position?.let {
            val smoothPosition = it - MILLS_BEFORE_SEEK_TO
            val playerPosition = if (smoothPosition > 0) smoothPosition else it

            player.seekTo(playerPosition)
        }
        player.start()
    }

    override fun stop() {
        player.stop()
        player.reset()
    }

    override fun pause(): Int {
        player.pause()
        return player.currentPosition
    }
}

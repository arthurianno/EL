package com.elta.android.data.features.consultant.media

import android.net.Uri

interface AudioPlayer {
    fun prepare(uri: Uri)
    fun play(position: Int? = null)
    fun stop()
    fun pause(): Int
    fun getPlaybackTime(): Int
}

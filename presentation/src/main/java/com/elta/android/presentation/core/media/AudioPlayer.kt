package com.elta.android.presentation.core.media

import android.net.Uri

interface AudioPlayer {
    val volumeList: List<Float>
    fun prepare(uri: Uri)
    fun play(position: Int? = null)
    fun stop()
    fun pause(): Int
}

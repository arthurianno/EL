package com.elta.android.domain.features.consultant.repository

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface AudioPlayerRepository {
    val trackPosition: Flow<Int>
    fun start(uri: Uri, trackPosition: Int? = null)
    fun stop(time: Long)
    fun pause(): Int

}
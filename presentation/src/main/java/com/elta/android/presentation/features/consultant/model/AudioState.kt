package com.elta.android.presentation.features.consultant.model

import javax.annotation.concurrent.Immutable

@Immutable
data class AudioState(
    val isPlaying: Boolean,
    val duration: Int,
    val trackPosition: Int
)

package com.elta.android.presentation.features.consultant.viewmodel

import android.net.Uri
import com.elta.android.domain.common.usecase.GetTrackPositionUseCase
import com.elta.android.domain.common.usecase.PauseAudioTrackUseCase
import com.elta.android.domain.common.usecase.PlayAudioTrackUseCase
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch

/** Owns one playback subscription at a time; the ViewModel owns the message UI state. */
class ConsultantAudioPlayer @Inject constructor(
    private val playAudioTrack: PlayAudioTrackUseCase,
    private val pauseAudioTrack: PauseAudioTrackUseCase,
    private val trackPosition: GetTrackPositionUseCase
) {
    private var positionJob: Job? = null

    fun play(
        scope: CoroutineScope,
        uri: Uri,
        position: Int?,
        onPosition: (Int) -> Unit,
        onFinished: () -> Unit
    ) {
        positionJob?.cancel()
        playAudioTrack(uri, position)
        positionJob = scope.launch {
            var previousPosition: Int? = null
            trackPosition()
                .takeWhile { currentPosition ->
                    if (previousPosition == currentPosition) {
                        onFinished()
                        false
                    } else {
                        previousPosition = currentPosition
                        true
                    }
                }
                .collect(onPosition)
        }
    }

    fun pause(): Int {
        positionJob?.cancel()
        positionJob = null
        return pauseAudioTrack()
    }
}

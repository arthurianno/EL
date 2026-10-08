package com.elta.android.presentation.features.consultant.viewmodel

import android.net.Uri
import com.elta.android.domain.common.usecase.AudioVolumeUseCase
import com.elta.android.domain.common.usecase.StartAudioRecordUseCase
import com.elta.android.domain.common.usecase.StopAudioRecordUseCase
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/** Owns the recorder and its volume subscription; the ViewModel owns recording UI state. */
class ConsultantVoiceRecorder @Inject constructor(
    private val startRecord: StartAudioRecordUseCase,
    private val stopRecord: StopAudioRecordUseCase,
    private val volume: AudioVolumeUseCase
) {
    private var volumeJob: Job? = null

    fun start(scope: CoroutineScope, onStarted: (Uri) -> Unit, onLevel: (Float) -> Unit) {
        volumeJob?.cancel()
        val uri = startRecord()
        onStarted(uri)
        volumeJob = scope.launch { volume().collect(onLevel) }
    }

    fun stop() {
        stopRecord()
        volumeJob?.cancel()
        volumeJob = null
    }
}

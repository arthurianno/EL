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
import timber.log.Timber

/** Owns the recorder and its volume subscription; the ViewModel owns recording UI state. */
class ConsultantVoiceRecorder @Inject constructor(
    private val startRecord: StartAudioRecordUseCase,
    private val stopRecord: StopAudioRecordUseCase,
    private val volume: AudioVolumeUseCase
) {
    private var volumeJob: Job? = null
    private var isRecording = false

    fun start(scope: CoroutineScope, onStarted: (Uri) -> Unit, onLevel: (Float) -> Unit) {
        if (isRecording) return
        volumeJob?.cancel()
        val uri = startRecord()
        isRecording = true
        onStarted(uri)
        volumeJob = scope.launch { volume().collect(onLevel) }
    }

    fun stop(): Boolean {
        if (!isRecording) return false
        isRecording = false
        volumeJob?.cancel()
        volumeJob = null
        return try {
            stopRecord()
            true
        } catch (error: RuntimeException) {
            Timber.e(error, "Failed to stop consultant voice recording")
            false
        }
    }
}

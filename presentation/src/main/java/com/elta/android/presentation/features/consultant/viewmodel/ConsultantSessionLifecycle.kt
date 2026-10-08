package com.elta.android.presentation.features.consultant.viewmodel

import androidx.lifecycle.Lifecycle
import com.elta.android.domain.features.consultant.model.WebimUser
import com.elta.android.domain.features.consultant.usecase.WebimSessionUseCase
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ConsultantSessionLifecycle internal constructor(
    private val createSession: suspend (WebimUser) -> Unit,
    private val resumeSession: () -> Unit,
    private val pauseSession: () -> Unit,
    private val destroySession: () -> Unit
) {
    @Inject constructor(session: WebimSessionUseCase) : this(
        createSession = session::create,
        resumeSession = session::onResume,
        pauseSession = session::onPause,
        destroySession = session::onDestroy
    )

    private var creationJob: Job? = null
    private var resumeJob: Job? = null
    private var isStarted = false

    fun onEvent(event: Lifecycle.Event, scope: CoroutineScope, user: WebimUser) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                creationJob?.cancel()
                creationJob = scope.launch { createSession(user) }
            }

            Lifecycle.Event.ON_START -> {
                isStarted = true
                resumeJob?.cancel()
                resumeJob = scope.launch {
                    creationJob?.join()
                    if (!isStarted) return@launch

                    try {
                        resumeSession()
                    } catch (error: IllegalStateException) {
                        createSession(user)
                        if (isStarted) resumeSession()
                    }
                }
            }

            Lifecycle.Event.ON_PAUSE -> {
                isStarted = false
                resumeJob?.cancel()
                pauseSession()
            }

            Lifecycle.Event.ON_DESTROY -> {
                isStarted = false
                resumeJob?.cancel()
                creationJob?.cancel()
                destroySession()
            }

            else -> Unit
        }
    }
}

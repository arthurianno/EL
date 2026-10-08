package com.elta.android.presentation.features.consultant.viewmodel

import com.elta.android.domain.features.consultant.usecase.SendMessageUseCase
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Prevents the same message from being submitted twice while its request is active. */
class ConsultantMessageSender internal constructor(
    private val sendMessage: suspend (SendMessageUseCase.Params) -> Unit
) {
    @Inject constructor(sendMessage: SendMessageUseCase) : this(sendMessage::invoke)

    private val pending = mutableSetOf<SendMessageUseCase.Params>()

    fun send(
        scope: CoroutineScope,
        params: SendMessageUseCase.Params,
        onAccepted: () -> Unit = {},
        onFailure: (Exception) -> Unit
    ): Boolean {
        if (!pending.add(params)) return false

        scope.launch {
            var accepted = false
            try {
                sendMessage(params)
                accepted = true
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                onFailure(error)
            } finally {
                pending.remove(params)
            }
            if (accepted) onAccepted()
        }
        return true
    }
}

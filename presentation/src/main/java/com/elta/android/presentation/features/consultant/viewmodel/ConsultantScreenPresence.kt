package com.elta.android.presentation.features.consultant.viewmodel

import androidx.lifecycle.Lifecycle
import com.elta.android.common.utils.EltaMessageClient
import javax.inject.Inject

/** Controls notification suppression only while the chat is visible. */
class ConsultantScreenPresence internal constructor(
    private val setActive: (Boolean) -> Unit
) {
    @Inject constructor(messageClient: EltaMessageClient) : this(
        { active -> messageClient.isConsultantScreenActive = active }
    )

    fun onEvent(event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> setActive(true)
            Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_DESTROY -> leave()
            else -> Unit
        }
    }

    fun leave() {
        setActive(false)
    }
}

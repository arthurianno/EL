package com.elta.android.presentation.features.consultant.model

import com.elta.android.presentation.core.compose.common.Action

sealed class ConsultantAction : Action {
    object SearchClick : ConsultantAction()
    object FileClick : ConsultantAction()
    object VoiceClick : ConsultantAction()
}

package com.elta.android.presentation.features.consultant.model

import com.elta.android.domain.features.consultant.model.BotOption
import com.elta.android.presentation.core.compose.common.Action

sealed class ConsultantAction : Action {
    data class OptionClick(val option: BotOption) : ConsultantAction()
    data object BackClick : ConsultantAction()
    data object ResetClick : ConsultantAction()
    data class CopyMessageClick(val text: String) : ConsultantAction()
    data class SendTextClick(val text: String) : ConsultantAction()
}

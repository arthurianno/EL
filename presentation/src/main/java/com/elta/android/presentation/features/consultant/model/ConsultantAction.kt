package com.elta.android.presentation.features.consultant.model

import com.elta.android.presentation.core.compose.common.Action

sealed class ConsultantAction : Action {
    object SearchClick : ConsultantAction()
    object FileClick : ConsultantAction()
    object VoiceClick : ConsultantAction()
    data class SendMessageClick(val text: String) : ConsultantAction()
    data class ChatMessageClick(val message: ChatUiEntity) : ConsultantAction()
    data class ChatMessageLongClick(val message: ChatUiEntity) : ConsultantAction()
    object SelectPhotoClick : ConsultantAction()
    object MakePhotoClick : ConsultantAction()
    object SelectFileClick : ConsultantAction()
    object PreviewBackPressure : ConsultantAction()
    object PreviewSendClick : ConsultantAction()
}

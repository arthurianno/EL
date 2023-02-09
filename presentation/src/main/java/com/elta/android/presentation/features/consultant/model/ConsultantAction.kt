package com.elta.android.presentation.features.consultant.model

import android.net.Uri
import com.elta.android.presentation.core.compose.common.Action

sealed class ConsultantAction : Action {
    object SearchClick : ConsultantAction()
    object FileClick : ConsultantAction()
    object StartRecVoiceClick : ConsultantAction()
    object StopRecVoiceClick : ConsultantAction()
    object DeleteRecVoiceClick : ConsultantAction()
    data class SendVoiceRecClick(val uri: Uri) : ConsultantAction()
    data class SendMessageClick(val text: String) : ConsultantAction()
    data class ChatMessageClick(val message: ChatUiEntity) : ConsultantAction()
    data class ChatMessageLongClick(val message: ChatUiEntity) : ConsultantAction()
    object SelectPhotoClick : ConsultantAction()
    object MakePhotoClick : ConsultantAction()
    object SelectFileClick : ConsultantAction()
    object PreviewBackPressure : ConsultantAction()
    object PreviewSendClick : ConsultantAction()
    object ClearCache : ConsultantAction()
}

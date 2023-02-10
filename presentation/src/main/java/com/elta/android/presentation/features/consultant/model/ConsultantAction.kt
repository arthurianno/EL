package com.elta.android.presentation.features.consultant.model

import com.elta.android.presentation.core.compose.common.Action
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus

sealed class ConsultantAction : Action {
    object SearchClick : ConsultantAction()
    object FileClick : ConsultantAction()

    @OptIn(ExperimentalPermissionsApi::class)
    data class StartRecVoiceClick(val permissionStatus: PermissionStatus) : ConsultantAction()
    object StopRecVoiceClick : ConsultantAction()
    object DeleteRecVoiceClick : ConsultantAction()
    object SendVoiceRecClick : ConsultantAction()
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

package com.elta.android.presentation.features.consultant.model

import android.net.Uri
import com.elta.android.presentation.core.compose.common.Action
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus

sealed class ConsultantAction : Action {
    data object FileClick : ConsultantAction()
    data class VerifyFile(val message: MessageUiEntity) : ConsultantAction()

    @OptIn(ExperimentalPermissionsApi::class)
    data class VoiceRecordClick(val permissionStatus: PermissionStatus) : ConsultantAction()
    data object StopRecordVoiceClick : ConsultantAction()
    data object DeleteRecordVoiceClick : ConsultantAction()

    data class PlayAudioClick(val message: MessageUiEntity) : ConsultantAction()
    data class PauseAudioClick(val message: MessageUiEntity) : ConsultantAction()
    data class OnAudioTrackClick(val message: MessageUiEntity, val percentOfTrack: Float) : ConsultantAction()

    data object CancelPhotoClick : ConsultantAction()
    data object SendVoiceRecordClick : ConsultantAction()
    data object SendMessageClick : ConsultantAction()
    data class SendAutoMessage(val text: String) : ConsultantAction()
    data class InputChanged(val text: String) : ConsultantAction()
    data class ChatMessageClick(val message: MessageUiEntity) : ConsultantAction()

    data class ChatMessageLongClick(val message: MessageUiEntity) :
        ConsultantAction()

    @OptIn(ExperimentalPermissionsApi::class)
    data class SelectPhotoClick(val permissionStatus: PermissionStatus) : ConsultantAction()

    @OptIn(ExperimentalPermissionsApi::class)
    data class MakePhotoClick(val permissionStatus: PermissionStatus) : ConsultantAction()

    @OptIn(ExperimentalPermissionsApi::class)
    data class SelectFileClick(val permissionStatus: PermissionStatus) : ConsultantAction()
    data object PreviewBackPressure : ConsultantAction()
    data object PictureSendClick : ConsultantAction()
    data class FileSelected(val uri: Uri) : ConsultantAction()
    data class PictureSelected(val uri: Uri) : ConsultantAction()

    data object PhotoTaken : ConsultantAction()

    data class SelectedOperatorRate(val number: Int) : ConsultantAction()
    data class CopyMessageClick(val message: MessageUiEntity) : ConsultantAction()
    data class DeleteMessageClick(val message: MessageUiEntity) : ConsultantAction()
    data class EditMessageClick(val message: MessageUiEntity) : ConsultantAction()

    data object OnDismissContextMenu : ConsultantAction()

    data object SaveEditMessageClick : ConsultantAction()

    data object CancelEditMessageClick : ConsultantAction()

    data object OnDownIconClick : ConsultantAction()

    data object OnSwipeRefresh : ConsultantAction()
}

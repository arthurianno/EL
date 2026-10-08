package com.elta.android.presentation.features.consultant.viewmodel

import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.features.consultant.model.ConsultantAction
import com.elta.android.presentation.features.consultant.model.ConsultantViewState

internal object ConsultantStateReducer {
    fun reduce(state: ConsultantViewState, action: Action): ConsultantViewState = when (action) {
        is ConsultantAction.InputChanged -> state.copy(inputMessage = action.text)
        is ConsultantAction.SelectPhotoClick,
        is ConsultantAction.MakePhotoClick,
        is ConsultantAction.SelectFileClick -> state.copy(isOpenBottomSheet = false)

        is ConsultantAction.PhotoTaken -> state.copy(
            previewState = state.previewState.copy(
                isPhotoPreview = true,
                isFromCamera = true
            )
        )

        ConsultantAction.FileClick -> state.copy(isOpenBottomSheet = true)
        is ConsultantAction.OnDismissContextMenu -> state.copy(
            contextMenuEntity = state.contextMenuEntity.copy(isOpenContextMenu = false)
        )

        is ConsultantAction.ChatMessageLongClick -> state.copy(
            contextMenuEntity = state.contextMenuEntity.copy(
                isOpenContextMenu = true,
                selectedMessage = action.message
            )
        )

        is ConsultantAction.EditMessageClick -> state.copy(
            inputMessage = action.message.text.orEmpty(),
            messageForEdit = action.message,
            isEditMessage = true,
            contextMenuEntity = state.contextMenuEntity.copy(
                isOpenContextMenu = false,
                selectedMessage = null
            )
        )

        else -> state
    }
}

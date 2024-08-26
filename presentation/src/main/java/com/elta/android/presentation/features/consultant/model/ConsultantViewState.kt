package com.elta.android.presentation.features.consultant.model

import android.net.Uri
import androidx.compose.runtime.Immutable
import com.elta.android.domain.features.consultant.model.WebimUser

@Immutable
data class ConsultantViewState(
    val inputMessage: String,
    val recordGraphState: RecordGraphState,
    val messageForEdit: MessageUiEntity?,
    val isEditMessage: Boolean,
    val bottomBarIconState: BottomBarIconState,
    val connectState: ConnectState,
    val chat: ChatUiEntity,
    val user: WebimUser,
    val previewState: PreviewState,
    val hasNewMessages: Boolean,
    val isOpenBottomSheet: Boolean,
    val audioFileUri: Uri,
    val contextMenuEntity: ContextMenuUiEntity,
    val isLoadingNextMessagesPage: Boolean
)

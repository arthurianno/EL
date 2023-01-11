package com.elta.android.presentation.features.consultant.model

import android.net.Uri
import androidx.compose.runtime.Immutable

@Immutable
data class ConsultantViewState(
    val webimConnectState: ConnectState,
    val chat: List<ChatUiEntity>,
    val previewPhoto: Uri,
    val hasNewMessages: Boolean,
    val isOpenBottomSheet: Boolean,
    val isPhotoPreview: Boolean
)

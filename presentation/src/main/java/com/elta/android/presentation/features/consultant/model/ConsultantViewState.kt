package com.elta.android.presentation.features.consultant.model

import android.net.Uri
import androidx.compose.runtime.Immutable
import com.elta.android.domain.features.consultant.model.WebimUser

@Immutable
data class ConsultantViewState(
    val webimConnectState: ConnectState,
    val chat: List<ChatUiEntity>,
    val previewPhoto: Uri,
    val user: WebimUser,
    val hasNewMessages: Boolean,
    val isOpenBottomSheet: Boolean,
    val isPhotoPreview: Boolean
)

package com.elta.android.presentation.features.newsChannel.model

import com.elta.android.presentation.core.compose.common.Action
import com.google.accompanist.permissions.ExperimentalPermissionsApi

sealed class NewsAction : Action {
    data object FileClick : NewsAction()
    data class VerifyFile(val message: MessageUiEntity) : NewsAction()

    object OnUpIconClick : NewsAction()

    data class InputChanged(val text: String) : NewsAction()
    data class ChatMessageClick(val message: MessageUiEntity) : NewsAction()

    data class ChatMessageLongClick(val message: MessageUiEntity) : NewsAction()

    @OptIn(ExperimentalPermissionsApi::class)
    data object PreviewBackPressure : NewsAction()

    data class CopyMessageClick(val message: MessageUiEntity) : NewsAction()

    data class DownloadImage(val imageData: String?) : NewsAction() // Изменено
    data class ShareImage(val imageData: String?) : NewsAction() // Изменено

    data object OnDismissContextMenu : NewsAction()

    data object OnDownIconClick : NewsAction()

    data object OnSwipeRefresh : NewsAction()

    object LoadNextPage : NewsAction()
}

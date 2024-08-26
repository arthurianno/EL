package com.elta.android.presentation.features.consultant.model

import androidx.compose.runtime.Immutable

@Immutable
data class ContextMenuUiEntity(
    val isOpenContextMenu: Boolean,
    val selectedMessage: MessageUiEntity?
)

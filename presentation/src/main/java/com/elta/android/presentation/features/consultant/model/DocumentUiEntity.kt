package com.elta.android.presentation.features.consultant.model

import android.net.Uri

data class DocumentUiEntity(
    val fileName: String,
    val fileType: MessageType,
    val url: String?,
    val size: Double?,
    val isPortrait: Boolean?,
    val cachingState: CachingState,
    val uri: Uri?
)

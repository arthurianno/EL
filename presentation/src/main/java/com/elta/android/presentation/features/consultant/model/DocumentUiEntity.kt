package com.elta.android.presentation.features.consultant.model

import android.net.Uri

// В модели данных добавить готовые размеры
data class DocumentUiEntity(
    val fileName: String,
    val fileType: MessageType,
    val url: String?,
    val base64Data: String? = null,
    val size: Double?,
    val isPortrait: Boolean?,
    val cachingState: CachingState,
    val uri: Uri?,
    val width: Int? = null,
    val height: Int? = null,
    // Добавить флаг что размеры уже вычислены
    val areDimensionsCalculated: Boolean = false
)

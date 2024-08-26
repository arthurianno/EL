package com.elta.android.domain.common.model

import com.elta.android.domain.features.consultant.model.ContentType

data class FileInfo(
    val id: Long,
    val name: String,
    val timestamp: Long,
    val type: ContentType
)

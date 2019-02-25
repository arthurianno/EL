package com.elta.android.domain.features.diary.tags.model

import java.util.Date

data class Tag(
    val id: String,
    val name: String,
    val image: TagImage,
    val isReadOnly: Boolean,
    val modificationTime: Date?
)
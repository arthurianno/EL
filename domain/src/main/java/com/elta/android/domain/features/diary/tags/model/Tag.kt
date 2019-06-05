package com.elta.android.domain.features.diary.tags.model

data class Tag(
    val id: String,
    val name: String,
    val image: TagImage,
    val isReadOnly: Boolean,
    val modificationTime: Long?
)
package com.elta.android.data.features.diary.events.dto.v2

data class MedicamentDto(
    val id: Long,
    val name: String,
    val deleted: Boolean,
    val other: Boolean,
    val touchedAt: Long
)

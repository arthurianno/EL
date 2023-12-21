package com.elta.android.domain.features.diary.medicines.model

data class Medicament(
    val id: Long,
    val name: String,
    val isDeleted: Boolean,
    val isOther: Boolean,
    val touchedAt: Long
)

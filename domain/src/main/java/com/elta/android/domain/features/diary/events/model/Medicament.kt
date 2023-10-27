package com.elta.android.domain.features.diary.events.model

data class Medicament(
    val id: Int,
    val name: String,
    val insulinType: MedicamentInsulinType,
    val deleted: Boolean
)

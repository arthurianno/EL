package com.elta.android.domain.features.diary.medicines.model

data class InsulinMedicament(
    val id: Int,
    val name: String,
    val insulinType: MedicamentInsulinType,
    val deleted: Boolean
)

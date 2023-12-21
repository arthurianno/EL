package com.elta.android.domain.features.diary.medicines.model

data class InsulinMedicamentStatistic(
    val bolusInsulinTypes: List<MedicamentInsulinType>,
    val basalInsulinTypes: List<MedicamentInsulinType>
)

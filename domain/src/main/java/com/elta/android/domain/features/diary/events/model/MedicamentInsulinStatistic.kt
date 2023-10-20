package com.elta.android.domain.features.diary.events.model

data class MedicamentInsulinStatistic(
    val bolusInsulinTypes: List<MedicamentInsulinType>,
    val basalInsulinTypes: List<MedicamentInsulinType>
)

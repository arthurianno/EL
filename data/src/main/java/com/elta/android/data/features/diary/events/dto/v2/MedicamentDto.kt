package com.elta.android.data.features.diary.events.dto.v2

data class MedicamentDto(
    val id: Int,
    val name: String,
    val insulinType: MedicamentInsulinTypeDto,
    val deleted: Boolean
) {
    data class MedicamentInsulinTypeDto(
        val code: String,
        val id: Int,
        val name: String
    )
}
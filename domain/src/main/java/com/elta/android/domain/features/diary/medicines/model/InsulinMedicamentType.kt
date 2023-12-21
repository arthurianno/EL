package com.elta.android.domain.features.diary.medicines.model

data class MedicamentInsulinType(
    val code: String,
    val id: Int,
    val name: String,
) {
    companion object {

        fun allMedicament() = MedicamentInsulinType(
            code = ALL,
            id = -1,
            name = ALL_NAME,
        )

        fun nullMedicament() = MedicamentInsulinType(
            code = "",
            id = -2,
            name = "",
        )
    }
}

const val ALL = "ALL"
const val ALL_NAME = "Все"
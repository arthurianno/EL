package com.elta.android.domain.features.diary.events.model

data class Drug(
        val id: Int,
        val insulinType: InsulinType,
        val name: String
) {
    data class InsulinType(
            val code: String,
            val id: Int,
            val name: String
    )
}
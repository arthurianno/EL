package com.elta.android.domain.features.diary.events.model

data class Insulin(
    val name: String,
    val drug: String,
    val type: InsulinType
)

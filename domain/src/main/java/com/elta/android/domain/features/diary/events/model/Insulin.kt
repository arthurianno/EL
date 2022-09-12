package com.elta.android.domain.features.diary.events.model

data class Insulin(
    val previousName: String,
    val drug: String,
    val type: InsulinType
)

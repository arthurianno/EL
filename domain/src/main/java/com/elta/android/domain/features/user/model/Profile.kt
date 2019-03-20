package com.elta.android.domain.features.user.model

import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings

data class Profile(
    val firstName: String,
    val secondName: String,
    val gender: Gender,
    val email: String,
    val glucoseLevelSettings: GlucoseLevelSettings,
    val diabetes: Diabetes,
    val weight: Double?,
    val hba1cLevel: Double?
)
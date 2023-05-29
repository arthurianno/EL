package com.elta.android.domain.features.user.model

data class ProfileSettings(
    val isOnboarded: Boolean,
    val glucoseFormat: GlucoseFormat
)

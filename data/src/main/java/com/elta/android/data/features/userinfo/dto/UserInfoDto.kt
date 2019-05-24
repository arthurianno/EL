package com.elta.android.data.features.userinfo.dto

data class UserInfoDto(
    val id: Long?,
    val isUserLoggedIn: Boolean,
    val isFeedbackSent: Boolean,
    val isEmailConfirmed: Boolean,
    val isOnboardingPassed: Boolean
)
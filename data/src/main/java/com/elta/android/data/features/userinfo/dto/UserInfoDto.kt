package com.elta.android.data.features.userinfo.dto

data class UserInfoDto(
    val id: Long? = null,
    val isUserLoggedIn: Boolean? = null,
    val isFeedbackSent: Boolean? = null,
    val isEmailConfirmed: Boolean? = null,
    val isOnboardingPassed: Boolean? = null
)
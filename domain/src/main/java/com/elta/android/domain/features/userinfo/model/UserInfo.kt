package com.elta.android.domain.features.userinfo.model

data class UserInfo(
    val id: Long,
    val isUserLoggedIn: Boolean,
    var isFeedbackSent: Boolean,
    val isEmailConfirmed: Boolean,
    var isOnBoardingPassed: Boolean
)
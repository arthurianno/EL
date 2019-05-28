package com.elta.android.domain.features.userinfo.model

data class UserInfo(
    val isUserLoggedIn: Boolean? = null,
    var isFeedbackSent: Boolean? = null,
    val isEmailConfirmed: Boolean? = null,
    var isOnBoardingPassed: Boolean? = null
)
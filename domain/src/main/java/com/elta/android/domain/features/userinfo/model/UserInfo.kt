package com.elta.android.domain.features.userinfo.model

/**
 * Represents local information about user.
 *
 * To update particular property of #UserInfo create new instance using named arguments.
 * For example: to update #UserInfo.isFeedbackSent create UserInfo(isFeedbackSent = true).
 */
data class UserInfo(
    val isUserLoggedIn: Boolean = false,
    val isFeedbackSent: Boolean = false,
    val isEmailConfirmed: Boolean = false,
    val isOnBoardingPassed: Boolean = false,
    val isFirstHomeEntrance: Boolean = false
)

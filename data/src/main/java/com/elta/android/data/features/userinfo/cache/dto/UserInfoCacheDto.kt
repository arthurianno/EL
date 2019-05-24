package com.elta.android.data.features.userinfo.cache.dto

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class UserInfoCacheDto(
    @Id(assignable = true) var id: Long,
    val isUserLoggedIn: Boolean,
    val isFeedbackSent: Boolean,
    val isEmailConfirmed: Boolean,
    val isOnboardingPassed: Boolean
)
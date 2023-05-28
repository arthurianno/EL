package com.elta.android.data.features.userinfo.cache.dto

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class UserInfoDbEntity(
    @Id(assignable = true) var id: Long,
    val isUserLoggedIn: Boolean = false,
    val isFeedbackSent: Boolean = false,
    val isEmailConfirmed: Boolean = false,
    val isOnboardingPassed: Boolean = false,
    val isFirstHomeEntrance: Boolean = false,
    val isFirstSync: Boolean = false,
)

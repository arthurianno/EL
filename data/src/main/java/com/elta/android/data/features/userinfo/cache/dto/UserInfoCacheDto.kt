package com.elta.android.data.features.userinfo.cache.dto

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class UserInfoCacheDto(
    @Id(assignable = true) var id: Long,
    val isUserLoggedIn: Boolean? = null,
    val isFeedbackSent: Boolean? = null,
    val isEmailConfirmed: Boolean? = null,
    val isOnboardingPassed: Boolean? = null,
    var isFirstHomeEntrance: Boolean? = null
)

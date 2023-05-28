package com.elta.android.data.features.userinfo.mapper // ktlint-disable filename

import com.elta.android.data.features.userinfo.cache.dto.UserInfoDbEntity
import com.elta.android.domain.features.userinfo.model.UserInfo

internal fun UserInfoDbEntity.toDomain(): UserInfo =
    UserInfo(
        isUserLoggedIn = isUserLoggedIn,
        isFeedbackSent = isFeedbackSent,
        isEmailConfirmed = isEmailConfirmed,
        isFirstHomeEntrance = isFirstHomeEntrance,
        isFirstSync = isFirstSync
    )

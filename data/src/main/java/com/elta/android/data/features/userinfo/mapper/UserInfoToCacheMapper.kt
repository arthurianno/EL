package com.elta.android.data.features.userinfo.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.userinfo.cache.dto.UserInfoCacheDto
import com.elta.android.data.features.userinfo.dto.UserInfoDto
import javax.inject.Inject

class UserInfoToCacheMapper @Inject constructor() : Mapper<UserInfoDto, UserInfoCacheDto> {

    override fun mapFromObject(source: UserInfoDto): UserInfoCacheDto =
        with(source) {
            UserInfoCacheDto(
                id = id ?: 0L,
                isUserLoggedIn = isUserLoggedIn,
                isEmailConfirmed = isEmailConfirmed,
                isFeedbackSent = isFeedbackSent,
                isOnboardingPassed = isOnboardingPassed
            )
        }
}
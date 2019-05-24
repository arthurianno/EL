package com.elta.android.data.features.userinfo.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.userinfo.cache.dto.UserInfoCacheDto
import com.elta.android.data.features.userinfo.dto.UserInfoDto
import javax.inject.Inject

class UserInfoFromCacheMapper @Inject constructor() : Mapper<UserInfoCacheDto, UserInfoDto> {

    override fun mapFromObject(source: UserInfoCacheDto): UserInfoDto =
        with(source) {
            UserInfoDto(
                id = id,
                isUserLoggedIn = isUserLoggedIn,
                isEmailConfirmed = isEmailConfirmed,
                isFeedbackSent = isFeedbackSent,
                isOnboardingPassed = isOnboardingPassed
            )
        }
}
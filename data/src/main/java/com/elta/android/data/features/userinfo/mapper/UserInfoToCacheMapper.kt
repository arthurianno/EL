package com.elta.android.data.features.userinfo.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.userinfo.cache.dto.UserInfoCacheDto
import com.elta.android.domain.features.userinfo.model.UserInfo
import javax.inject.Inject

class UserInfoToCacheMapper @Inject constructor() : Mapper<UserInfo, UserInfoCacheDto> {

    override fun mapFromObject(source: UserInfo): UserInfoCacheDto =
        with(source) {
            UserInfoCacheDto(
                id = id,
                isUserLoggedIn = isUserLoggedIn,
                isEmailConfirmed = isEmailConfirmed,
                isFeedbackSent = isFeedbackSent,
                isOnboardingPassed = isOnBoardingPassed
            )
        }
}
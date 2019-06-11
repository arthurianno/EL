package com.elta.android.data.features.userinfo.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.userinfo.cache.dto.UserInfoCacheDto
import com.elta.android.domain.features.userinfo.model.UserInfo
import javax.inject.Inject

class UserInfoToDomainMapper @Inject constructor() : Mapper<UserInfoCacheDto, UserInfo> {

    override fun mapFromObject(source: UserInfoCacheDto): UserInfo =
        with(source) {
            UserInfo(
                isUserLoggedIn = isUserLoggedIn,
                isEmailConfirmed = isEmailConfirmed,
                isFeedbackSent = isFeedbackSent,
                isOnBoardingPassed = isOnboardingPassed,
                isFirstHomeEntrance = isFirstHomeEntrance
            )
        }
}
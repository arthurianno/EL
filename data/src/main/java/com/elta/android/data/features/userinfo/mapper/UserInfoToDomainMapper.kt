package com.elta.android.data.features.userinfo.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.userinfo.dto.UserInfoDto
import com.elta.android.domain.features.userinfo.model.UserInfo
import javax.inject.Inject

class UserInfoToDomainMapper @Inject constructor() : Mapper<UserInfoDto, UserInfo> {

    override fun mapFromObject(source: UserInfoDto): UserInfo =
        with(source) {
            UserInfo(
                isUserLoggedIn = isUserLoggedIn ?: false,
                isEmailConfirmed = isEmailConfirmed ?: false,
                isFeedbackSent = isFeedbackSent ?: false,
                isOnboardingPassed = isOnboardingPassed ?: false
            )
        }
}
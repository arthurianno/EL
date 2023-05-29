package com.elta.android.data.features.auth.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.auth.model.SocialUserDto
import com.elta.android.domain.features.auth.model.SocialUser
import javax.inject.Inject

class SocialUserDtoMapper @Inject constructor() : Mapper<SocialUserDto, SocialUser> {
    override fun mapFromObject(source: SocialUserDto): SocialUser =
        with(source) {
            SocialUser(name)
        }
}

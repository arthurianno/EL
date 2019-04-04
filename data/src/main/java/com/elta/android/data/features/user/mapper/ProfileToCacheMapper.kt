package com.elta.android.data.features.user.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.user.cache.dto.ProfileCacheDto
import com.elta.android.data.features.user.dto.ProfileDto
import javax.inject.Inject

class ProfileToCacheMapper @Inject constructor() : Mapper<ProfileDto, ProfileCacheDto> {

    override fun mapFromObject(source: ProfileDto): ProfileCacheDto =
        with(source) {
            ProfileCacheDto(
                id = email?.hashCode()?.toLong() ?: 0L,
                diabetes = diabetes?.name,
                weight = weight,
                gender = gender?.name,
                email = email,
                timeStamp = timeStamp,
                firstName = person?.firstName,
                lastName = person?.lastName,
                minValue = glucoseLevel?.minValue,
                maxValue = glucoseLevel?.maxValue
            )
        }
}
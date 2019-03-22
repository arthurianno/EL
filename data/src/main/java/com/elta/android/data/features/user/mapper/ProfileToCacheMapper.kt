package com.elta.android.data.features.user.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.storage.UserHolder
import com.elta.android.data.features.user.cache.dto.ProfileCacheDto
import com.elta.android.data.features.user.dto.ProfileDto
import javax.inject.Inject

class ProfileToCacheMapper @Inject constructor(
    private val userHolder: UserHolder
) : Mapper<ProfileDto, ProfileCacheDto> {
    override fun mapFromObject(source: ProfileDto): ProfileCacheDto =
        with(source) {
            ProfileCacheDto(
                id = userHolder.currentUser ?: 0,
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
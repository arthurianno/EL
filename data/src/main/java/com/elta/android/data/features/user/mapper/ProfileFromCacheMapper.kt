package com.elta.android.data.features.user.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.user.cache.dto.ProfileCacheDto
import com.elta.android.data.features.user.dto.DiabetTypeDto
import com.elta.android.data.features.user.dto.GenderTypeDto
import com.elta.android.data.features.user.dto.GlucoseLevelDto
import com.elta.android.data.features.user.dto.PersonDto
import com.elta.android.data.features.user.dto.ProfileDto
import javax.inject.Inject

class ProfileFromCacheMapper @Inject constructor() : Mapper<ProfileCacheDto, ProfileDto> {
    override fun mapFromObject(source: ProfileCacheDto): ProfileDto =
        with(source) {
            ProfileDto(
                diabetes = diabetes?.let { DiabetTypeDto.valueOf(it) },
                weight = weight,
                gender = gender?.let { GenderTypeDto.valueOf(it) },
                person = PersonDto(
                    firstName = firstName,
                    lastName = lastName
                ),
                glucoseLevel = GlucoseLevelDto(
                    minValue = minValue,
                    maxValue = maxValue
                ),
                email = email,
                timeStamp = timeStamp
            )
        }
}
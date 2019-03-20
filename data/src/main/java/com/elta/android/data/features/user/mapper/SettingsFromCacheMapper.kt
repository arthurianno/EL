package com.elta.android.data.features.user.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.user.cache.dto.SettingsCacheDto
import com.elta.android.data.features.user.dto.DiabetTypeDto
import com.elta.android.data.features.user.dto.GenderTypeDto
import com.elta.android.data.features.user.dto.GlucoseLevelDto
import com.elta.android.data.features.user.dto.PersonDto
import com.elta.android.data.features.user.dto.ProfileDto
import javax.inject.Inject

class SettingsFromCacheMapper @Inject constructor() : Mapper<SettingsCacheDto, ProfileDto> {
    override fun mapFromObject(source: SettingsCacheDto): ProfileDto =
        with(source) {
            ProfileDto(
                diabetType = DiabetTypeDto.valueOf(diabetType),
                weight = weight,
                gender = GenderTypeDto.valueOf(gender),
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
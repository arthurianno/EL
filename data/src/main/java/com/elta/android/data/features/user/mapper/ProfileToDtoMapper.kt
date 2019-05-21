package com.elta.android.data.features.user.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.user.dto.DiabetTypeDto
import com.elta.android.data.features.user.dto.GenderTypeDto
import com.elta.android.data.features.user.dto.GlucoseLevelDto
import com.elta.android.data.features.user.dto.PersonDto
import com.elta.android.data.features.user.dto.ProfileDto
import com.elta.android.domain.features.user.model.Profile
import javax.inject.Inject

class ProfileToDtoMapper @Inject constructor() : Mapper<Profile, ProfileDto> {

    override fun mapFromObject(source: Profile): ProfileDto =
        with(source) {
            ProfileDto(
                diabetes = diabetes?.let { DiabetTypeDto.valueOf(it.name) },
                weight = weight,
                gender = gender?.let { GenderTypeDto.valueOf(it.name) },
                person = if (firstName == null && secondName == null) null else {
                    PersonDto(
                        firstName = firstName,
                        lastName = secondName
                    )
                },
                glucoseLevel = glucoseLevelSettings?.let {
                    GlucoseLevelDto(
                        minValue = it.normal.start,
                        maxValue = it.normal.end
                    )
                },
                email = email,
                socialNetworks = null,
                timeStamp = timeStamp
            )
        }
}
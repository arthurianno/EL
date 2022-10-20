package com.elta.android.data.features.user.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.user.dto.DiabetTypeDto
import com.elta.android.data.features.user.dto.GenderTypeDto
import com.elta.android.data.features.user.dto.GlucoseLevelDto
import com.elta.android.data.features.user.dto.HealthAppDto
import com.elta.android.data.features.user.dto.PersonDto
import com.elta.android.data.features.user.dto.ProfileDto
import com.elta.android.domain.features.user.model.Gender
import com.elta.android.domain.features.user.model.HealthApp
import com.elta.android.domain.features.user.model.Profile
import javax.inject.Inject

class ProfileToDtoMapper @Inject constructor(
    private val healthAppsMapper: Mapper<HealthApp, HealthAppDto>
) : Mapper<Profile, ProfileDto> {

    override fun mapFromObject(source: Profile): ProfileDto =
        with(source) {
            ProfileDto(
                diabetes = diabetes?.let { DiabetTypeDto.valueOf(it.name) },
                weight = weight,
                gender = gender.toDto(),
                person = if (firstName == null && secondName == null) null else {
                    PersonDto(
                        firstName = firstName,
                        lastName = secondName
                    )
                },
                glucoseLevelsAverage = GlucoseLevelDto(
                    minValue = glucoseLevelSettings.normal.start,
                    maxValue = glucoseLevelSettings.normal.end
                ),
                glucoseLevelsBeforeEating = GlucoseLevelDto(
                    minValue = glucoseLevelBeforeEatSettings.normal.start,
                    maxValue = glucoseLevelBeforeEatSettings.normal.end
                ),
                glucoseLevelsAfterEating = GlucoseLevelDto(
                    minValue = glucoseLevelAfterEatSettings.normal.start,
                    maxValue = glucoseLevelAfterEatSettings.normal.end
                ),
                email = email,
                socialNetworks = null,
                healthApps = healthApps?.let { healthAppsMapper.mapFromObjects(it) },
                timeStamp = timeStamp
            )
        }

    private fun Gender.toDto(): GenderTypeDto? =
        when (this) {
            Gender.MALE,
            Gender.FEMALE -> GenderTypeDto.valueOf(name)

            Gender.NOT_SPECIFIED -> null
        }
}

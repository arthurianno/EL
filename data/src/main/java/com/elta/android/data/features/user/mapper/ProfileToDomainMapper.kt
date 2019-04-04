package com.elta.android.data.features.user.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.user.dto.GlucoseLevelDto
import com.elta.android.data.features.user.dto.ProfileDto
import com.elta.android.data.features.user.dto.SocialNetworkDto
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.domain.features.user.model.Diabetes
import com.elta.android.domain.features.user.model.Gender
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.domain.features.user.model.SocialNetwork
import javax.inject.Inject

class ProfileToDomainMapper @Inject constructor(
    private val networksMapper: Mapper<SocialNetworkDto, SocialNetwork>
) : Mapper<ProfileDto, Profile> {

    override fun mapFromObject(source: ProfileDto): Profile =
        with(source) {
            Profile(
                diabetes = diabetes?.let { Diabetes.valueOf(it.name) },
                weight = weight,
                gender = gender?.let { Gender.valueOf(it.name) },
                firstName = person?.firstName,
                secondName = person?.lastName,
                glucoseLevelSettings = glucoseLevel.toSettings(),
                email = email,
                socialNetworks = socialNetworks?.let { networksMapper.mapFromObjects(it) },
                timeStamp = timeStamp
            )
        }

    private fun GlucoseLevelDto?.toSettings(): GlucoseLevelSettings {
        return if (this == null || this.minValue == null || this.maxValue == null) {
            GlucoseLevelSettings()
        } else {
            GlucoseLevelSettings.fromNormalValues(this.minValue, this.maxValue)
        }
    }
}
package com.elta.android.data.features.user.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.user.cache.dto.HealthAppCacheDto
import com.elta.android.data.features.user.cache.dto.NetworkCacheDto
import com.elta.android.data.features.user.cache.dto.ProfileCacheDto
import com.elta.android.data.features.user.dto.DiabetTypeDto
import com.elta.android.data.features.user.dto.GenderTypeDto
import com.elta.android.data.features.user.dto.GlucoseLevelDto
import com.elta.android.data.features.user.dto.HealthAppDto
import com.elta.android.data.features.user.dto.PersonDto
import com.elta.android.data.features.user.dto.ProfileDto
import com.elta.android.data.features.user.dto.SocialNetworkDto
import timber.log.Timber
import javax.inject.Inject

class ProfileFromCacheMapper @Inject constructor(
    private val mapper: Mapper<NetworkCacheDto, SocialNetworkDto>,
    private val healthAppsMapper: Mapper<HealthAppCacheDto, HealthAppDto>
) : Mapper<ProfileCacheDto, ProfileDto> {
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
                timeStamp = timeStamp,
                socialNetworks = mapper.mapFromObjects(socialNetworks),
                healthApps = healthAppsMapper.mapFromObjects(healthApps)
            )
        }
}
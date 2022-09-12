package com.elta.android.data.features.user.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.user.cache.dto.HealthAppCacheDto
import com.elta.android.data.features.user.cache.dto.NetworkCacheDto
import com.elta.android.data.features.user.cache.dto.ProfileCacheDto
import com.elta.android.data.features.user.dto.HealthAppDto
import com.elta.android.data.features.user.dto.ProfileDto
import com.elta.android.data.features.user.dto.SocialNetworkDto
import javax.inject.Inject

class ProfileToCacheMapper @Inject constructor(
    private val mapper: Mapper<SocialNetworkDto, NetworkCacheDto>,
    private val healthAppMapper: Mapper<HealthAppDto, HealthAppCacheDto>
) : Mapper<ProfileDto, ProfileCacheDto> {

    override fun mapFromObject(source: ProfileDto): ProfileCacheDto =
        with(source) {
            val profile = ProfileCacheDto(
                id = email?.hashCode()?.toLong() ?: 0L,
                diabetes = diabetes?.name,
                weight = weight,
                gender = gender?.name,
                email = email,
                timeStamp = timeStamp,
                firstName = person?.firstName,
                lastName = person?.lastName,
                minBeforeEatingValue = glucoseLevelsBeforeEating?.minValue,
                maxBeforeEatingValue = glucoseLevelsBeforeEating?.maxValue,
                minAfterEatingValue = glucoseLevelsAfterEating?.minValue,
                maxAfterEatingValue = glucoseLevelsAfterEating?.maxValue,
                minAverageValue = glucoseLevelsAverage?.minValue,
                maxAverageValue = glucoseLevelsAverage?.maxValue
            )
            profile.tempSocialNetworks = source.socialNetworks?.let {
                mapper.mapFromObjects(it)
            } ?: emptyList()
            profile.tempHealthApps = source.healthApps?.let {
                healthAppMapper.mapFromObjects(it)
            } ?: emptyList()

            profile
        }
}

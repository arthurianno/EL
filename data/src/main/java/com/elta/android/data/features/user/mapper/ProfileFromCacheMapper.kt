package com.elta.android.data.features.user.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.user.cache.dto.HealthAppCacheDto
import com.elta.android.data.features.user.cache.dto.NetworkCacheDto
import com.elta.android.data.features.user.cache.dto.ProfileCacheDto
import com.elta.android.data.features.user.dto.DiabetesTypeNetworkEntity
import com.elta.android.data.features.user.dto.GenderTypeNetworkEntity
import com.elta.android.data.features.user.dto.GlucoseLevelNetworkEntity
import com.elta.android.data.features.user.dto.HealthAppNetworkEntity
import com.elta.android.data.features.user.dto.PersonNetworkEntity
import com.elta.android.data.features.user.dto.ProfileNetworkResponse
import com.elta.android.data.features.user.dto.SocialNetworkDto
import javax.inject.Inject

class ProfileFromCacheMapper @Inject constructor(
    private val mapper: Mapper<NetworkCacheDto, SocialNetworkDto>,
    private val healthAppsMapper: Mapper<HealthAppCacheDto, HealthAppNetworkEntity>
) : Mapper<ProfileCacheDto, ProfileNetworkResponse> {
    override fun mapFromObject(source: ProfileCacheDto): ProfileNetworkResponse =
        with(source) {
            ProfileNetworkResponse(
                diabetes = diabetes?.let { DiabetesTypeNetworkEntity.getByItemName(it) },
                weight = weight,
                gender = gender?.let { GenderTypeNetworkEntity.valueOf(it) },
                person = PersonNetworkEntity(
                    firstName = firstName,
                    lastName = lastName
                ),
                glucoseLevelsBeforeEating = GlucoseLevelNetworkEntity(
                    minBeforeEatingValue,
                    maxBeforeEatingValue
                ),
                glucoseLevelsAfterEating = GlucoseLevelNetworkEntity(
                    minAfterEatingValue,
                    maxAfterEatingValue
                ),
                glucoseLevelsAverage = GlucoseLevelNetworkEntity(
                    minAverageValue,
                    maxAverageValue
                ),
                email = email,
                timeStamp = timeStamp,
                socialNetworks = mapper.mapFromObjects(socialNetworks),
                healthApps = healthAppsMapper.mapFromObjects(healthApps)
            )
        }
}

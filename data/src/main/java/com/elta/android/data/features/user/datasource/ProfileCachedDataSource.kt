package com.elta.android.data.features.user.datasource

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.user.cache.ProfileCache
import com.elta.android.data.features.user.cache.dto.ProfileCacheDto
import com.elta.android.data.features.user.dto.ProfileDto
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class ProfileCachedDataSource @Inject constructor(
    private val fromCacheMapper: Mapper<ProfileCacheDto, ProfileDto>,
    private val cache: ProfileCache
) : ProfileDataSource {

    override fun updateProfile(profile: ProfileDto): Completable =
        Completable.fromCallable {
            val profiles = cache.getAll(CommonConditions.All)
            if (profiles.isNotEmpty()) {
                val cachedProfile = profiles.first()
                val newProfile = cachedProfile.copy(
                    diabetes = profile.diabetes?.name ?: cachedProfile.diabetes,
                    weight = profile.weight ?: cachedProfile.weight,
                    gender = profile.gender?.name ?: cachedProfile.gender,
                    email = profile.email ?: cachedProfile.email,
                    timeStamp = profile.timeStamp,
                    firstName = profile.person?.firstName ?: cachedProfile.firstName,
                    lastName = profile.person?.lastName ?: cachedProfile.lastName,
                    minValue = profile.glucoseLevel?.minValue ?: cachedProfile.minValue,
                    maxValue = profile.glucoseLevel?.maxValue ?: cachedProfile.maxValue
                )
                cache.update(listOf(newProfile))
            }
        }

    override fun getUserProfile(): Single<ProfileDto> =
        Single.fromCallable {
            val profiles = cache.getAll(CommonConditions.All)
            if (profiles.isNotEmpty()) {
                profiles.first()
            } else {
                throw NoSuchElementException("Current user profile is empty.")
            }
        }.map(fromCacheMapper::mapFromObject)
}
package com.elta.android.data.features.user.datasource

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.common.storage.UserHolder
import com.elta.android.data.features.user.cache.dto.HealthAppCacheDto
import com.elta.android.data.features.user.cache.dto.NetworkCacheDto
import com.elta.android.data.features.user.cache.dto.ProfileCacheDto
import com.elta.android.data.features.user.dto.HealthAppDto
import com.elta.android.data.features.user.dto.ProfileDto
import com.elta.android.data.features.user.dto.SocialNetworkDto
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class ProfileCachedDataSource @Inject constructor(
    private val userHolder: UserHolder,
    private val profileFromCacheMapper: Mapper<ProfileCacheDto, ProfileDto>,
    private val profileToCacheMapper: Mapper<ProfileDto, ProfileCacheDto>,
    private val networkToCacheMapper: Mapper<SocialNetworkDto, NetworkCacheDto>,
    private val healthAppsCacheMapper: Mapper<HealthAppDto, HealthAppCacheDto>,
    private val profileCache: Cache<ProfileCacheDto>
) : ProfileDataSource {

    override fun updateProfile(profile: ProfileDto): Completable =
        Completable.fromCallable {
            userHolder.currentUser?.let {
                profileCache.get(CommonConditions.ById(it))?.let { cachedProfile ->
                    val newProfile = cachedProfile.copy(
                        diabetes = profile.diabetes?.name ?: cachedProfile.diabetes,
                        weight = profile.weight ?: cachedProfile.weight,
                        gender = profile.gender?.name ?: cachedProfile.gender,
                        email = profile.email ?: cachedProfile.email,
                        timeStamp = profile.timeStamp,
                        firstName = profile.person?.firstName ?: cachedProfile.firstName,
                        lastName = profile.person?.lastName ?: cachedProfile.lastName,
                        minBeforeEatingValue = profile.glucoseLevelsBeforeEating?.minValue
                            ?: cachedProfile.minBeforeEatingValue,
                        maxBeforeEatingValue = profile.glucoseLevelsBeforeEating?.maxValue
                            ?: cachedProfile.maxBeforeEatingValue,
                        minAfterEatingValue = profile.glucoseLevelsAfterEating?.minValue
                            ?: cachedProfile.minAfterEatingValue,
                        maxAfterEatingValue = profile.glucoseLevelsAfterEating?.maxValue
                            ?: cachedProfile.maxAfterEatingValue,
                        minAverageValue = profile.glucoseLevelsAverage?.minValue
                            ?: cachedProfile.minAverageValue,
                        maxAverageValue = profile.glucoseLevelsAverage?.maxValue
                            ?: cachedProfile.maxAverageValue
                    )
                    newProfile.tempSocialNetworks = profile.socialNetworks?.let { socials ->
                        networkToCacheMapper.mapFromObjects(socials)
                    } ?: cachedProfile.socialNetworks

                    newProfile.tempHealthApps = profile.healthApps?.let { health ->
                        healthAppsCacheMapper.mapFromObjects(health)
                    } ?: cachedProfile.healthApps

                    profileCache.update(listOf(newProfile))
                } ?: profileCache.add(listOf(profileToCacheMapper.mapFromObject(profile)))
            }
        }

    override fun getUserProfile(): Single<ProfileDto> =
        Single.fromCallable {
            userHolder.currentUser?.let {
                profileCache.get(CommonConditions.ById(it))
                    ?: throw NoSuchElementException("Current user profile is empty.")
            }
        }.map(profileFromCacheMapper::mapFromObject)

    override fun hasProfile(): Single<Boolean> = Single.fromCallable {
        userHolder.currentUser?.let {
            profileCache.contains(CommonConditions.ById(it))
        } ?: false
    }
}

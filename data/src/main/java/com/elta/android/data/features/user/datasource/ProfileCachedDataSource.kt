package com.elta.android.data.features.user.datasource

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.common.storage.UserHolder
import com.elta.android.data.features.user.cache.dto.ProfileCacheDto
import com.elta.android.data.features.user.dto.ProfileDto
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class ProfileCachedDataSource @Inject constructor(
    private val userHolder: UserHolder,
    private val profileFromCacheMapper: Mapper<ProfileCacheDto, ProfileDto>,
    private val profileToCacheMapper: Mapper<ProfileDto, ProfileCacheDto>,
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
                        minValue = profile.glucoseLevel?.minValue ?: cachedProfile.minValue,
                        maxValue = profile.glucoseLevel?.maxValue ?: cachedProfile.maxValue
                    )
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
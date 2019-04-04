package com.elta.android.data.features.user.datasource

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.common.storage.UserHolder
import com.elta.android.data.features.user.api.ProfileApi
import com.elta.android.data.features.user.cache.dto.NetworkCacheDto
import com.elta.android.data.features.user.cache.dto.ProfileCacheDto
import com.elta.android.data.features.user.dto.ProfileDto
import com.elta.android.data.features.user.dto.SocialNetworkDto
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class ProfileRemoteDataSource @Inject constructor(
    private val userHolder: UserHolder,
    private val profileToCacheMapper: Mapper<ProfileDto, ProfileCacheDto>,
    private val networkToCacheMapper: Mapper<SocialNetworkDto, NetworkCacheDto>,
    private val cache: Cache<ProfileCacheDto>,
    private val api: ProfileApi
) : ProfileDataSource {

    override fun updateProfile(profile: ProfileDto): Completable =
        api.updateUserSettings(profile)

    override fun getUserProfile(): Single<ProfileDto> =
        api.getUserSettings()
            .doOnSuccess(::saveLocalIfNeed)

    private fun saveLocalIfNeed(profileDto: ProfileDto) {
        val profileCacheDto = profileToCacheMapper.mapFromObject(profileDto)
        val networksCacheDto = profileDto.socialNetworks?.let { networkToCacheMapper.mapFromObjects(it) }
        cache.attach(profileCacheDto)
        networksCacheDto?.let { profileCacheDto.socialNetworks.addAll(it) }
        userHolder.currentUser?.let {
            cache.get(CommonConditions.ById(it))?.let { cached ->
                if (profileDto.timeStamp > cached.timeStamp) {
                    cache.update(listOf(profileCacheDto))
                }
            } ?: cache.add(listOf(profileCacheDto))
        }
    }
}
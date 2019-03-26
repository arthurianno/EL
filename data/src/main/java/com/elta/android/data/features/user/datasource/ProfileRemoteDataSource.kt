package com.elta.android.data.features.user.datasource

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.user.api.ProfileApi
import com.elta.android.data.features.user.cache.ProfileCache
import com.elta.android.data.features.user.cache.dto.ProfileCacheDto
import com.elta.android.data.features.user.dto.ProfileDto
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class ProfileRemoteDataSource @Inject constructor(
    private val toCacheMapper: Mapper<ProfileDto, ProfileCacheDto>,
    private val cache: ProfileCache,
    private val api: ProfileApi
) : ProfileDataSource {

    override fun updateProfile(profile: ProfileDto): Completable =
        api.updateUserSettings(profile)

    override fun getUserProfile(): Single<ProfileDto> =
        api.getUserSettings()
            .doOnSuccess(::saveLocalIfNeed)

    private fun saveLocalIfNeed(profileDto: ProfileDto) {
        val profiles = cache.get(CommonConditions.Empty)
        if (profiles.isNotEmpty()) {
            val cached = profiles.first()
            if (profileDto.timeStamp > cached.timeStamp) {
                cache.update(listOf(toCacheMapper.mapFromObject(profileDto)))
            }
        } else {
            cache.add(listOf(toCacheMapper.mapFromObject(profileDto)))
        }
    }
}
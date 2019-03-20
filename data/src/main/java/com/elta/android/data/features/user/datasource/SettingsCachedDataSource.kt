package com.elta.android.data.features.user.datasource

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.user.cache.SettingsCache
import com.elta.android.data.features.user.cache.dto.SettingsCacheDto
import com.elta.android.data.features.user.dto.ProfileDto
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class SettingsCachedDataSource @Inject constructor(
    private val toCacheMapper: Mapper<ProfileDto, SettingsCacheDto>,
    private val fromCacheMapper: Mapper<SettingsCacheDto, ProfileDto>,
    private val cache: SettingsCache) : SettingsDataSource {

    override fun updateUserProfile(gender: String?, weight: Double?, diabetes: String?): Completable =
    //todo realize
        Completable.complete()

    override fun getUserProfile(): Single<ProfileDto> =
        Single.fromCallable {
            cache.get(CommonConditions.All).first()
        }.map(fromCacheMapper::mapFromObject)
}
package com.elta.android.data.features.user.datasource

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.common.storage.UserHolder
import com.elta.android.data.features.user.api.ProfileApi
import com.elta.android.data.features.user.cache.dto.ProfileCacheDto
import com.elta.android.data.features.user.cache.dto.ProfileSettingsDbEntity
import com.elta.android.data.features.user.dto.ProfileNetworkResponse
import com.elta.android.data.features.user.dto.ProfileSettingsNetworkRequest
import com.elta.android.data.features.user.dto.ProfileSettingsNetworkResponse
import com.elta.android.data.features.user.mapper.toDb
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class ProfileRemoteDataSource @Inject constructor(
    private val userHolder: UserHolder,
    private val profileToCacheMapper: Mapper<ProfileNetworkResponse, ProfileCacheDto>,
    private val profileCache: Cache<ProfileCacheDto>,
    private val profileSettingsCache: Cache<ProfileSettingsDbEntity>,
    private val api: ProfileApi
) : ProfileDataSource {

    override fun updateProfile(profile: ProfileNetworkResponse): Completable =
        api.updateUserSettings(profile)

    @Suppress("MagicNumber")
    override fun getUserProfile(): Single<ProfileNetworkResponse> =
        api.getUserSettings()
            .doOnSuccess { userHolder.currentUser = it.email.hashCode().toLong() }
            .doOnSuccess(::saveLocalIfNeed)

    private fun saveLocalIfNeed(profileNetworkResponse: ProfileNetworkResponse) {
        val profileCacheDto = profileToCacheMapper.mapFromObject(profileNetworkResponse)
        userHolder.currentUser?.let {
            profileCache.get(CommonConditions.ById(it))?.let { cached ->
                if (profileNetworkResponse.timeStamp > cached.timeStamp) {
                    profileCache.update(listOf(profileCacheDto))
                }
            } ?: profileCache.add(listOf(profileCacheDto))
        }
    }

    override fun hasProfile(): Single<Boolean> {
        throw IllegalStateException("hasProfile available only for cached data source")
    }

    override fun getProfileSettings(): Single<ProfileSettingsNetworkResponse> =
        api.getProfileSettings()
            .doOnSuccess { profile ->
                userHolder.currentUser?.let { id ->
                    profileSettingsCache.update(listOf(profile.toDb(id)))
                }
            }

    override fun updateProfileSettings(settings: ProfileSettingsNetworkRequest): Completable =
        api.updateProfileSettings(settings)
}

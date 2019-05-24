package com.elta.android.data.features.userinfo.datasource

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.common.storage.UserHolder
import com.elta.android.data.features.userinfo.cache.dto.UserInfoCacheDto
import com.elta.android.data.features.userinfo.dto.UserInfoDto
import io.reactivex.Completable
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject

class UserInfoCachedDataSource @Inject constructor(
    private val dtoMapper: Mapper<UserInfoCacheDto, UserInfoDto>,
    private val cacheMapper: Mapper<UserInfoDto, UserInfoCacheDto>,
    private val userHolder: UserHolder,
    private val cache: Cache<UserInfoCacheDto>
) : UserInfoDataSource {

    override fun getUserInfo(): Single<UserInfoDto> =
        Single.fromCallable {
            userHolder.currentUser?.let {
                cache.get(CommonConditions.ById(it))
                    ?: throw NoSuchElementException("Current user is empty.")
            } ?: throw NoSuchElementException("Current user is not exist.")
        }
            .doOnError { Timber.e(it) }
            .map(dtoMapper::mapFromObject)

    override fun updateUserInfo(userInfo: UserInfoDto): Completable =
        Completable.fromCallable {
            cache.add(listOf(cacheMapper.mapFromObject(userInfo)))
        }
}
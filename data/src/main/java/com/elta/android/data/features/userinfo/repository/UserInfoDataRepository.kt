package com.elta.android.data.features.userinfo.repository

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.storage.UserHolder
import com.elta.android.data.features.userinfo.cache.dto.UserInfoCacheDto
import com.elta.android.data.features.userinfo.datasource.UserInfoDataSource
import com.elta.android.domain.features.userinfo.model.UserInfo
import com.elta.android.domain.features.userinfo.repository.UserInfoRepository
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class UserInfoDataRepository @Inject constructor(
    private val cacheMapper: Mapper<UserInfo, UserInfoCacheDto>,
    private val domainMapper: Mapper<UserInfoCacheDto, UserInfo>,
    private val userInfoDataSource: UserInfoDataSource,
    private val userHolder: UserHolder
) : UserInfoRepository {

    override fun getUserInfo(): Single<UserInfo> =
        userInfoDataSource.getUserInfo()
            .map { domainMapper.mapFromObject(it) }

    override fun updateUserInfo(userInfo: UserInfo): Completable =
        userInfoDataSource.updateUserInfo(cacheMapper.mapFromObject(userInfo))

    override fun setFeedbackSent(state: Boolean): Completable =
        userInfoDataSource.updateUserInfo(
            UserInfoCacheDto(
                id = checkNotNull(userHolder.currentUser),
                isFeedbackSent = state
            ))
}
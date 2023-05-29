package com.elta.android.data.features.userinfo.repository

import com.elta.android.data.features.userinfo.datasource.UserInfoDataSource
import com.elta.android.data.features.userinfo.mapper.toDomain
import com.elta.android.domain.features.userinfo.model.UserInfo
import com.elta.android.domain.features.userinfo.repository.UserInfoRepository
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class UserInfoDataRepository @Inject constructor(
    private val userInfoDataSource: UserInfoDataSource
) : UserInfoRepository {

    override fun getUserInfo(): Single<UserInfo> =
        userInfoDataSource.getUserInfo()
            .map { it.toDomain() }

    override fun updateUserInfo(userInfo: UserInfo): Completable =
        userInfoDataSource.updateUserInfo(userInfo)
}

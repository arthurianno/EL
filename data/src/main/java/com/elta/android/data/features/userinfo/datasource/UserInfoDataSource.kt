package com.elta.android.data.features.userinfo.datasource

import com.elta.android.data.features.userinfo.cache.dto.UserInfoCacheDto
import io.reactivex.Completable
import io.reactivex.Single

interface UserInfoDataSource {

    fun getUserInfo(): Single<UserInfoCacheDto>

    fun updateUserInfo(userInfo: UserInfoCacheDto): Completable
}
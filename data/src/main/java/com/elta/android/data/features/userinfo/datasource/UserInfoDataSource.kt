package com.elta.android.data.features.userinfo.datasource

import com.elta.android.data.features.userinfo.cache.dto.UserInfoDbEntity
import com.elta.android.domain.features.userinfo.model.UserInfo
import io.reactivex.Completable
import io.reactivex.Single

interface UserInfoDataSource {

    fun getUserInfo(): Single<UserInfoDbEntity>

    fun updateUserInfo(userInfo: UserInfo): Completable
}

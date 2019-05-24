package com.elta.android.data.features.userinfo.datasource

import com.elta.android.data.features.userinfo.dto.UserInfoDto
import io.reactivex.Completable
import io.reactivex.Single

interface UserInfoDataSource {

    fun getUserInfo(): Single<UserInfoDto>

    fun updateUserInfo(userInfo: UserInfoDto): Completable
}
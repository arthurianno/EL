package com.elta.android.domain.features.userinfo.repository

import com.elta.android.domain.features.userinfo.model.UserInfo
import io.reactivex.Single

interface UserInfoRepository {

    fun getUserInfo(): Single<UserInfo>
}
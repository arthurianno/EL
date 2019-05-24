package com.elta.android.data.features.userinfo.repository

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.userinfo.datasource.UserInfoDataSource
import com.elta.android.data.features.userinfo.dto.UserInfoDto
import com.elta.android.domain.features.userinfo.model.UserInfo
import com.elta.android.domain.features.userinfo.repository.UserInfoRepository
import io.reactivex.Single
import javax.inject.Inject

class UserInfoDataRepository @Inject constructor(
    private val mapper: Mapper<UserInfoDto, UserInfo>,
    private val userInfoDataSource: UserInfoDataSource
) : UserInfoRepository {

    override fun getUserInfo(): Single<UserInfo> =
        userInfoDataSource.getUserInfo()
            .map { mapper.mapFromObject(it) }
}
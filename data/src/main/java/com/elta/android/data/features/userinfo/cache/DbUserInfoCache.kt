package com.elta.android.data.features.userinfo.cache

import com.elta.android.data.features.common.cache.BoxCache
import com.elta.android.data.features.common.cache.BoxStoreFactory
import com.elta.android.data.features.userinfo.cache.dto.UserInfoCacheDto
import javax.inject.Inject

class DbUserInfoCache @Inject constructor(
    factory: BoxStoreFactory
) : BoxCache<UserInfoCacheDto>(factory) {

    override val classToken: Class<UserInfoCacheDto> = UserInfoCacheDto::class.java
}
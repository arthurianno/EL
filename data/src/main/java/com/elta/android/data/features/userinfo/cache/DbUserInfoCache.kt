package com.elta.android.data.features.userinfo.cache

import com.elta.android.data.features.common.cache.BoxCache
import com.elta.android.data.features.common.cache.BoxStoreFactory
import com.elta.android.data.features.userinfo.cache.dto.UserInfoDbEntity
import javax.inject.Inject

class DbUserInfoCache @Inject constructor(
    factory: BoxStoreFactory
) : BoxCache<UserInfoDbEntity>(factory) {

    override val classToken: Class<UserInfoDbEntity> = UserInfoDbEntity::class.java
}

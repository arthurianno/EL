package com.elta.android.data.features.user.cache

import com.elta.android.data.features.common.cache.BoxCache
import com.elta.android.data.features.common.cache.BoxStoreFactory
import com.elta.android.data.features.user.cache.dto.ProfileCacheDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DbProfileCache @Inject constructor(
    factory: BoxStoreFactory
) : BoxCache<ProfileCacheDto>(factory), ProfileCache {

    override val classToken: Class<ProfileCacheDto> = ProfileCacheDto::class.java
}
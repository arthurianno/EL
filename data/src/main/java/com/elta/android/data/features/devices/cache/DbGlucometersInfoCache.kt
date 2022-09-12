package com.elta.android.data.features.devices.cache

import com.elta.android.data.features.common.cache.BoxCache
import com.elta.android.data.features.common.cache.BoxStoreFactory
import com.elta.android.data.features.devices.cache.dto.GlucometerInfoCachedDto
import javax.inject.Inject

class DbGlucometersInfoCache @Inject constructor(
    factory: BoxStoreFactory
) : BoxCache<GlucometerInfoCachedDto>(factory) {

    override val classToken: Class<GlucometerInfoCachedDto> = GlucometerInfoCachedDto::class.java
}

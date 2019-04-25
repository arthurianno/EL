package com.elta.android.data.features.observers.cache

import com.elta.android.data.features.common.cache.BoxCache
import com.elta.android.data.features.common.cache.BoxStoreFactory
import com.elta.android.data.features.observers.cache.dto.ObserverCacheDto
import javax.inject.Inject

class DbObserverCache @Inject constructor(
    factory: BoxStoreFactory
) : BoxCache<ObserverCacheDto>(factory) {
    override val classToken: Class<ObserverCacheDto> = ObserverCacheDto::class.java
}
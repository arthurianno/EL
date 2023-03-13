package com.elta.android.data.features.observers.cache

import com.elta.android.data.features.common.cache.BoxCache
import com.elta.android.data.features.common.cache.BoxStoreFactory
import com.elta.android.data.features.observers.model.ObserverDbEntity
import javax.inject.Inject

class DbObserverCache @Inject constructor(
    factory: BoxStoreFactory
) : BoxCache<ObserverDbEntity>(factory) {
    override val classToken: Class<ObserverDbEntity> = ObserverDbEntity::class.java
}

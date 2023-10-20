package com.elta.android.data.features.diary.insulin.cache.insulin

import com.elta.android.data.features.common.cache.BoxCache
import com.elta.android.data.features.common.cache.BoxStoreFactory
import javax.inject.Inject

class DbInsulinTypeCache @Inject constructor(
    factory: BoxStoreFactory
) : BoxCache<InsulinTypeDbEntity>(factory) {

    override val classToken: Class<InsulinTypeDbEntity> = InsulinTypeDbEntity::class.java
}

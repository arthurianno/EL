package com.elta.android.data.features.diary.medicines.cache

import com.elta.android.data.features.common.cache.BoxCache
import com.elta.android.data.features.common.cache.BoxStoreFactory
import com.elta.android.data.features.diary.medicines.cache.entity.InsulinTypeDbEntity
import javax.inject.Inject

class DbInsulinTypeCache @Inject constructor(
    factory: BoxStoreFactory
) : BoxCache<InsulinTypeDbEntity>(factory) {

    override val classToken: Class<InsulinTypeDbEntity> = InsulinTypeDbEntity::class.java
}

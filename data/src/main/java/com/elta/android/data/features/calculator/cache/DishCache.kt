package com.elta.android.data.features.calculator.cache

import com.elta.android.data.features.calculator.cache.model.DishDbEntity
import com.elta.android.data.features.common.cache.BoxCache
import com.elta.android.data.features.common.cache.BoxStoreFactory
import javax.inject.Inject

class DishCache @Inject constructor(
    factory: BoxStoreFactory
) : BoxCache<DishDbEntity>(factory) {
    override val classToken: Class<DishDbEntity> = DishDbEntity::class.java
}

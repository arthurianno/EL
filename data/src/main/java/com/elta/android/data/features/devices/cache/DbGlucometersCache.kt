package com.elta.android.data.features.devices.cache

import com.elta.android.data.features.common.cache.BoxCache
import com.elta.android.data.features.common.cache.BoxStoreFactory
import com.elta.android.data.features.common.cache.Condition
import com.elta.android.data.features.devices.cache.dto.GlucometerCachedDto
import com.elta.android.data.features.devices.cache.dto.GlucometerCachedDto_
import io.objectbox.kotlin.query
import javax.inject.Inject

class DbGlucometersCache @Inject constructor(
    factory: BoxStoreFactory
) : BoxCache<GlucometerCachedDto>(factory) {

    override val classToken: Class<GlucometerCachedDto> = GlucometerCachedDto::class.java

    override fun get(condition: Condition): GlucometerCachedDto? =
        when (condition) {
            is GlucometersConditions.Primary -> getPrimary()
            else -> super.get(condition)
        }

    private fun getPrimary(): GlucometerCachedDto? =
        box.query {
            equal(GlucometerCachedDto_.isPrimary, true)
        }.findFirst()
}

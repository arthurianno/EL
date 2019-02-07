package com.elta.android.data.features.sale_points.cache

import com.elta.android.data.features.sale_points.cache.dto.SalePointCacheDto
import io.objectbox.BoxStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DbSalePointsCache @Inject constructor(
    boxStore: BoxStore
) : SalePointsCache {

    private val box = boxStore.boxFor(SalePointCacheDto::class.java)

    override fun add(points: List<SalePointCacheDto>) {
        box.put(points)
    }

    override fun update(points: List<SalePointCacheDto>) {
        box.put(points)
    }

    override fun delete(points: List<SalePointCacheDto>) {
        box.remove(points)
    }

    override fun getAll(): List<SalePointCacheDto> = box.all
}
package com.elta.android.data.features.sale_points.cache

import com.elta.android.data.features.sale_points.cache.dto.SalePointCacheDto

interface SalePointsCache {

    fun add(points: List<SalePointCacheDto>)

    fun update(points: List<SalePointCacheDto>)

    fun delete(points: List<SalePointCacheDto>)

    fun getAll(): List<SalePointCacheDto>
}
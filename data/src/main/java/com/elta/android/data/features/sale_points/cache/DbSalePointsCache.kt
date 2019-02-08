package com.elta.android.data.features.sale_points.cache

import com.elta.android.data.features.sale_points.cache.dto.SalePointCacheDto
import com.elta.android.data.features.sale_points.cache.dto.SalePointCacheDto_
import io.objectbox.BoxStore
import io.objectbox.kotlin.query
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

    override fun getAllInBounds(
        southWestLatitude: Double,
        southWestLongitude: Double,
        northEastLatitude: Double,
        northEastLongitude: Double
    ): List<SalePointCacheDto> {
        val query = box.query {
            between(SalePointCacheDto_.latitude, southWestLatitude, northEastLatitude)
            if (southWestLongitude <= northEastLongitude) {
                between(SalePointCacheDto_.longitude, southWestLongitude, northEastLongitude)
            } else {
                greater(SalePointCacheDto_.longitude, southWestLongitude)
                or()
                less(SalePointCacheDto_.longitude, northEastLongitude)
                or()
                equal(SalePointCacheDto_.longitude, northEastLongitude, TOLERANCE)
            }
        }
        return query.find()
    }

    private companion object {
        const val TOLERANCE = 1E-5 // represents meter accuracy
    }
}
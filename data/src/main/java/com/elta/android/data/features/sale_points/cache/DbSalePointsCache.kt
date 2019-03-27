package com.elta.android.data.features.sale_points.cache

import com.elta.android.data.features.common.cache.BoxCache
import com.elta.android.data.features.common.cache.BoxScope
import com.elta.android.data.features.common.cache.BoxStoreFactory
import com.elta.android.data.features.common.cache.Condition
import com.elta.android.data.features.sale_points.cache.dto.SalePointCacheDto
import com.elta.android.data.features.sale_points.cache.dto.SalePointCacheDto_
import io.objectbox.kotlin.query
import io.objectbox.query.QueryBuilder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DbSalePointsCache @Inject constructor(
    factory: BoxStoreFactory
) : BoxCache<SalePointCacheDto>(factory) {

    override val classToken: Class<SalePointCacheDto> = SalePointCacheDto::class.java
    override val scope: BoxScope = BoxScope.PER_APP

    override fun getAll(condition: Condition): List<SalePointCacheDto> =
        when (condition) {
            is SalePointsConditions.Bounds -> getAllInBounds(
                southWestLatitude = condition.southWestLatitude,
                southWestLongitude = condition.southWestLongitude,
                northEastLatitude = condition.northEastLatitude,
                northEastLongitude = condition.northEastLongitude
            )
            is SalePointsConditions.Query -> getAllByQuery(condition.query)
            else -> super.getAll(condition)
        }

    @Suppress("LongMethod")
    private fun getAllInBounds(
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

    private fun getAllByQuery(query: String): List<SalePointCacheDto> {
        return if (query.isEmpty()) {
            emptyList()
        } else {
            val regex = Regex(TWO_AND_MORE_SPACES)
            val tokens = query.toLowerCase().trim().replace(regex, SPACE).split(SPACE)
            val builder = box.query()
            tokens.forEachIndexed { index, token ->
                builder.contains(SalePointCacheDto_.fullAddress, token, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                if (tokens.size > 1 && index != tokens.size - 1) {
                    builder.and()
                }
            }
            builder.build().find()
        }
    }

    private companion object {
        const val TOLERANCE = 1E-5 // represents meter accuracy
        const val SPACE = " "
        const val TWO_AND_MORE_SPACES = "[ ]{2,}"
    }
}
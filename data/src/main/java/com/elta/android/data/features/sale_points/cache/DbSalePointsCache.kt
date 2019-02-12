package com.elta.android.data.features.sale_points.cache

import com.elta.android.data.features.sale_points.cache.dto.SalePointCacheDto
import com.elta.android.data.features.sale_points.cache.dto.SalePointCacheDto_
import io.objectbox.BoxStore
import io.objectbox.kotlin.query
import io.objectbox.query.QueryBuilder
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DbSalePointsCache @Inject constructor(
    boxStore: BoxStore
) : SalePointsCache {

    private val box = boxStore.boxFor(SalePointCacheDto::class.java)

    override fun add(points: List<SalePointCacheDto>) {
        Timber.d("add ${Thread.currentThread().name}")
        box.put(points)
    }

    override fun update(points: List<SalePointCacheDto>) {
        box.put(points)
    }

    override fun delete(points: List<SalePointCacheDto>) {
        box.remove(points)
    }

    override fun get(condition: Condition): List<SalePointCacheDto> {
        return when (condition) {
            is SalePointsConditions.All -> box.all
            is SalePointsConditions.Bounds -> getAllInBounds(
                southWestLatitude = condition.southWestLatitude,
                southWestLongitude = condition.southWestLongitude,
                northEastLatitude = condition.northEastLatitude,
                northEastLongitude = condition.northEastLongitude
            )
            is SalePointsConditions.Query -> getAllByQuery(condition.query)
            else -> throw IllegalArgumentException("Passed condition $condition not supported.")
        }
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
        if (query.isEmpty()) {
            return emptyList()
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
            val start = System.currentTimeMillis()
            val result = builder.build().find()
            val end = System.currentTimeMillis()
            Timber.d("getAllByQuery $query; time ${end - start}")
            return result
        }
    }

    private companion object {
        const val TOLERANCE = 1E-5 // represents meter accuracy
        const val SPACE = " "
        const val TWO_AND_MORE_SPACES = "[ ]{2,}"
    }
}
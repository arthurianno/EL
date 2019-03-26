package com.elta.android.data.features.sale_points.cache

import com.elta.android.data.features.common.cache.BoxStoreFactory
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.common.cache.Condition
import com.elta.android.data.features.common.cache.IllegalDeleteConditionError
import com.elta.android.data.features.common.cache.IllegalGetConditionError
import com.elta.android.data.features.sale_points.cache.dto.SalePointCacheDto
import com.elta.android.data.features.sale_points.cache.dto.SalePointCacheDto_
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.query
import io.objectbox.query.QueryBuilder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DbSalePointsCache @Inject constructor(
    private val factory: BoxStoreFactory
) : SalePointsCache {

    private val box: Box<SalePointCacheDto>
        get() = factory.getBoxStore(BoxStoreFactory.DbScope.PER_APP).boxFor()

    override fun add(objects: List<SalePointCacheDto>) {
        box.put(objects)
    }

    override fun update(objects: List<SalePointCacheDto>) {
        box.put(objects)
    }

    override fun delete(condition: Condition) {
        when (condition) {
            is CommonConditions.All -> box.removeAll()
            is CommonConditions.ByIds -> box.removeByKeys(condition.ids)
            else -> throw IllegalDeleteConditionError(condition)
        }
    }

    override fun get(condition: Condition): List<SalePointCacheDto> =
        when (condition) {
            is CommonConditions.All -> box.all
            is SalePointsConditions.Bounds -> getAllInBounds(
                southWestLatitude = condition.southWestLatitude,
                southWestLongitude = condition.southWestLongitude,
                northEastLatitude = condition.northEastLatitude,
                northEastLongitude = condition.northEastLongitude
            )
            is SalePointsConditions.Query -> getAllByQuery(condition.query)
            else -> throw IllegalGetConditionError(condition)
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
            return builder.build().find()
        }
    }

    private companion object {
        const val TOLERANCE = 1E-5 // represents meter accuracy
        const val SPACE = " "
        const val TWO_AND_MORE_SPACES = "[ ]{2,}"
    }
}
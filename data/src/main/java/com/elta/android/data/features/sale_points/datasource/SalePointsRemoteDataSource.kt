package com.elta.android.data.features.sale_points.datasource

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.common.checkNetwork
import com.elta.android.data.features.sale_points.api.SalePointsApi
import com.elta.android.data.features.sale_points.cache.SalePointsCache
import com.elta.android.data.features.sale_points.cache.dto.SalePointCacheDto
import com.elta.android.data.features.sale_points.dto.MetaDto
import com.elta.android.data.features.sale_points.dto.SalePointDto
import com.elta.android.data.features.sale_points.dto.SalePointsDto
import com.elta.android.data.features.sale_points.dto.StateDto
import com.elta.android.data.features.sale_points.storage.SyncStorage
import com.nullgr.core.date.toTimestamp
import com.nullgr.core.hardware.NetworkChecker
import io.reactivex.Observable
import java.util.Date
import javax.inject.Inject

class SalePointsRemoteDataSource @Inject constructor(
    private val toCacheMapper: Mapper<SalePointDto, SalePointCacheDto>,
    private val salePointsCache: SalePointsCache,
    private val syncStorage: SyncStorage,
    private val checker: NetworkChecker,
    private val api: SalePointsApi
) : SalePointsDataSource {

    override fun getSalePoints(): Observable<List<SalePointDto>> =
        getSalePointsByPage(PAGE, PAGE_SIZE).checkNetwork(checker)
            .doOnNext { syncStorage.lastSync = Date().toTimestamp() }
            .map(SalePointsDto::points)
            .doOnNext(::updateCache)

    override fun getSalePoints(
        southWestLatitude: Double,
        southWestLongitude: Double,
        northEastLatitude: Double,
        northEastLongitude: Double
    ): Observable<List<SalePointDto>> = getSalePoints()

    override fun searchSalePoints(query: String): Observable<List<SalePointDto>> = getSalePoints()

    private fun getSalePointsByPage(page: Int, size: Int): Observable<SalePointsDto> =
        api.getSalePoints(syncStorage.lastSync, page, size)
            .switchMap { points ->
                val meta = points.meta
                val nextPage = meta.currentPage + 1
                when (meta.isTheLastPage()) {
                    true -> Observable.just(points)
                    else -> Observable.just(points).concatWith(getSalePointsByPage(nextPage, meta.pageSize))
                }
            }
            .collectInto(mutableListOf<SalePointsDto>()) { list, points -> list.add(points) }
            .map { list ->
                val allPoints = list.map { it.points }.flatten()
                val lastMeta = list.last().meta
                SalePointsDto(allPoints, lastMeta)
            }
            .toObservable()

    private fun MetaDto.isTheLastPage(): Boolean = currentPage * pageSize >= totalItems

    private fun updateCache(points: List<SalePointDto>) {
        val states = mutableMapOf<StateDto, MutableList<SalePointDto>>()
        points.forEach { point ->
            var state = states[point.modifiedState]
            if (state == null) {
                state = mutableListOf()
                states[point.modifiedState] = state
            }
            state.add(point)
        }
        states.forEach { entry ->
            val pointsForCache = toCacheMapper.mapFromObjects(entry.value)
            when (entry.key) {
                StateDto.CREATED -> salePointsCache.add(pointsForCache)
                StateDto.DELETED -> salePointsCache.delete(pointsForCache)
                StateDto.UPDATED -> salePointsCache.update(pointsForCache)
            }
        }
    }

    private companion object {
        const val PAGE = 1
        const val PAGE_SIZE = 500
    }
}
package com.elta.android.data.features.sale_points.datasource

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.cache.updateCache
import com.elta.android.data.features.common.isTheLastPage
import com.elta.android.data.features.common.storage.SyncStorage
import com.elta.android.data.features.sale_points.api.SalePointsApi
import com.elta.android.data.features.sale_points.cache.SalePointsCache
import com.elta.android.data.features.sale_points.cache.dto.SalePointCacheDto
import com.elta.android.data.features.sale_points.dto.SalePointDto
import com.elta.android.data.features.sale_points.dto.SalePointsDto
import com.nullgr.core.date.toTimestamp
import io.reactivex.Observable
import java.util.Date
import javax.inject.Inject

class SalePointsRemoteDataSource @Inject constructor(
    private val toCacheMapper: Mapper<SalePointDto, SalePointCacheDto>,
    private val salePointsCache: SalePointsCache,
    private val syncStorage: SyncStorage,
    private val api: SalePointsApi
) : SalePointsDataSource {

    override fun getSalePoints(): Observable<List<SalePointDto>> =
        getDataByPage(PAGE, PAGE_SIZE)
            .doOnNext { syncStorage.lastSalePointsSync = Date().toTimestamp() }
            .map(SalePointsDto::points)
            .doOnNext { points -> updateCache(points, salePointsCache, toCacheMapper) }

    override fun getSalePoints(
        southWestLatitude: Double,
        southWestLongitude: Double,
        northEastLatitude: Double,
        northEastLongitude: Double
    ): Observable<List<SalePointDto>> = getSalePoints()

    override fun searchSalePoints(query: String): Observable<List<SalePointDto>> = getSalePoints()

    private fun getDataByPage(page: Int, size: Int): Observable<SalePointsDto> =
        api.getSalePoints(syncStorage.lastSalePointsSync, page, size)
            .concatMap { data ->
                val meta = data.meta
                val nextPage = meta.currentPage + 1
                when (meta.isTheLastPage()) {
                    true -> Observable.just(data)
                    else -> Observable.just(data).concatWith(getDataByPage(nextPage, meta.pageSize))
                }
            }
            .collectInto(mutableListOf<SalePointsDto>()) { list, data -> list.add(data) }
            .map { list ->
                val allData = list.map { it.points }.flatten()
                val lastMeta = list.last().meta
                SalePointsDto(allData, lastMeta)
            }
            .toObservable()

    private companion object {
        const val PAGE = 1
        const val PAGE_SIZE = 500
    }
}
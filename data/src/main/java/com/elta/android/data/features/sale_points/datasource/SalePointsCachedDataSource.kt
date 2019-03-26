package com.elta.android.data.features.sale_points.datasource

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.sale_points.cache.SalePointsCache
import com.elta.android.data.features.sale_points.cache.SalePointsConditions
import com.elta.android.data.features.sale_points.cache.dto.SalePointCacheDto
import com.elta.android.data.features.sale_points.dto.SalePointDto
import io.reactivex.Observable
import javax.inject.Inject

class SalePointsCachedDataSource @Inject constructor(
    private val fromCacheMapper: Mapper<SalePointCacheDto, SalePointDto>,
    private val cache: SalePointsCache
) : SalePointsDataSource {

    override fun getSalePoints(): Observable<List<SalePointDto>> =
        Observable.fromCallable {
            cache.getAll(CommonConditions.All)
        }.map(fromCacheMapper::mapFromObjects)

    override fun getSalePoints(
        southWestLatitude: Double,
        southWestLongitude: Double,
        northEastLatitude: Double,
        northEastLongitude: Double
    ): Observable<List<SalePointDto>> =
        Observable.fromCallable {
            cache.getAll(
                SalePointsConditions.Bounds(
                    southWestLatitude = southWestLatitude,
                    southWestLongitude = southWestLongitude,
                    northEastLatitude = northEastLatitude,
                    northEastLongitude = northEastLongitude
                )
            )
        }.map(fromCacheMapper::mapFromObjects)

    override fun searchSalePoints(query: String): Observable<List<SalePointDto>> =
        Observable.fromCallable {
            cache.getAll(SalePointsConditions.Query(query))
        }.map(fromCacheMapper::mapFromObjects)
}
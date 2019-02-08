package com.elta.android.data.features.sale_points.repository

import com.elta.android.common.di.qualifires.Cache
import com.elta.android.common.di.qualifires.Remote
import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.sale_points.datasource.SalePointsDataSource
import com.elta.android.data.features.sale_points.dto.SalePointDto
import com.elta.android.domain.features.sale_points.model.CoordinatesBounds
import com.elta.android.domain.features.sale_points.model.SalePoint
import com.elta.android.domain.features.sale_points.repository.SalePointsRepository
import io.reactivex.Observable
import javax.inject.Inject

class SalePointsDataRepository @Inject constructor(
    private val toDomainMapper: Mapper<SalePointDto, SalePoint>,
    @Remote private val remoteSource: SalePointsDataSource,
    @Cache private val cacheSource: SalePointsDataSource
) : SalePointsRepository {

    override fun getSalePoints(): Observable<List<SalePoint>> =
        remoteSource.getSalePoints()
            .switchMap {
                cacheSource.getSalePoints()
                    .map { toDomainMapper.mapFromObjects(it) }
            }

    override fun getSalePoints(bounds: CoordinatesBounds): Observable<List<SalePoint>> =
        remoteSource.getSalePoints(
            southWestLatitude = bounds.southWest.latitude,
            southWestLongitude = bounds.southWest.longitude,
            northEastLatitude = bounds.northEast.latitude,
            northEastLongitude = bounds.northEast.longitude
        )
            .switchMap {
                cacheSource.getSalePoints(
                    southWestLatitude = bounds.southWest.latitude,
                    southWestLongitude = bounds.southWest.longitude,
                    northEastLatitude = bounds.northEast.latitude,
                    northEastLongitude = bounds.northEast.longitude
                ).map { toDomainMapper.mapFromObjects(it) }
            }
}
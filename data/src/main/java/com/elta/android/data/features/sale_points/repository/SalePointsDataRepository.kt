package com.elta.android.data.features.sale_points.repository

import com.elta.android.common.di.qualifires.Cache
import com.elta.android.common.di.qualifires.Remote
import com.elta.android.common.mapper.Mapper
import com.elta.android.data.common.onConnectionErrorReturnsEmpty
import com.elta.android.data.features.sale_points.datasource.SalePointsDataSource
import com.elta.android.data.features.sale_points.dto.SalePointDto
import com.elta.android.domain.features.sale_points.model.CoordinatesBounds
import com.elta.android.domain.features.sale_points.model.SalePoint
import com.elta.android.domain.features.sale_points.model.Type
import com.elta.android.domain.features.sale_points.repository.SalePointsRepository
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

class SalePointsDataRepository @Inject constructor(
    private val toDomainMapper: Mapper<SalePointDto, SalePoint>,
    @Remote private val remoteSource: SalePointsDataSource,
    @Cache private val cacheSource: SalePointsDataSource
) : SalePointsRepository {

    override fun getSalePoints(type: Type): Observable<List<SalePoint>> =
        cacheSource.getSalePoints(type.name)
            .flatMap {
                when (it.isEmpty()) {
                    true -> sync().andThen(cacheSource.getSalePoints(type.name))
                    else -> Observable.just(it)
                }
            }
            .map { toDomainMapper.mapFromObjects(it) }

    override fun getSalePoints(bounds: CoordinatesBounds): Observable<List<SalePoint>> =
        cacheSource.getSalePoints(
            southWestLatitude = bounds.southWest.latitude,
            southWestLongitude = bounds.southWest.longitude,
            northEastLatitude = bounds.northEast.latitude,
            northEastLongitude = bounds.northEast.longitude
        ).map { toDomainMapper.mapFromObjects(it) }

    override fun searchSalePoints(query: String, type: Type): Observable<List<SalePoint>> =
        cacheSource.searchSalePoints(query, type.name)
            .map { toDomainMapper.mapFromObjects(it) }

    override fun sync(): Completable =
        remoteSource.getSalePoints(null)
            .onConnectionErrorReturnsEmpty()
            .ignoreElements()
}

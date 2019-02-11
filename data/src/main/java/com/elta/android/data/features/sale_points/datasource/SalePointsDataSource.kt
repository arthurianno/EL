package com.elta.android.data.features.sale_points.datasource

import com.elta.android.data.features.sale_points.dto.SalePointDto
import io.reactivex.Observable

interface SalePointsDataSource {

    fun getSalePoints(): Observable<List<SalePointDto>>

    fun getSalePoints(
        southWestLatitude: Double,
        southWestLongitude: Double,
        northEastLatitude: Double,
        northEastLongitude: Double
    ): Observable<List<SalePointDto>>

    fun searchSalePoints(query: String): Observable<List<SalePointDto>>
}
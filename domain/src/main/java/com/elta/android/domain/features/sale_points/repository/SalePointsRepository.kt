package com.elta.android.domain.features.sale_points.repository

import com.elta.android.domain.features.sale_points.model.CoordinatesBounds
import com.elta.android.domain.features.sale_points.model.SalePoint
import io.reactivex.Observable

interface SalePointsRepository {

    fun getSalePoints(): Observable<List<SalePoint>>

    fun getSalePoints(bounds: CoordinatesBounds): Observable<List<SalePoint>>
}
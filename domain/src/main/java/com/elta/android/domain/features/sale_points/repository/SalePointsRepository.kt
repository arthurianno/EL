package com.elta.android.domain.features.sale_points.repository

import com.elta.android.domain.features.sale_points.model.CoordinatesBounds
import com.elta.android.domain.features.sale_points.model.SalePoint
import com.elta.android.domain.features.sale_points.model.Type
import io.reactivex.Completable
import io.reactivex.Observable

interface SalePointsRepository {

    fun getSalePoints(type: Type): Observable<List<SalePoint>>

    fun getSalePoints(bounds: CoordinatesBounds): Observable<List<SalePoint>>

    fun searchSalePoints(query: String, type: Type): Observable<List<SalePoint>>

    fun sync(): Completable
}
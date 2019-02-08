package com.elta.android.data.features.sale_points.datasource

import com.elta.android.data.features.sale_points.dto.SalePointDto
import io.reactivex.Observable

interface SalePointsDataSource {

    fun getSalePoints(): Observable<List<SalePointDto>>
}
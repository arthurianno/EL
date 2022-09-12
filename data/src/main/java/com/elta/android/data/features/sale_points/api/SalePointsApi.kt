package com.elta.android.data.features.sale_points.api

import com.elta.android.data.features.sale_points.dto.SalePointsDto
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface SalePointsApi {

    @GET("api/salepoints/v1/points")
    fun getSalePoints(
        @Query("lastTimestampSync") lastSync: Long?,
        @Query("pageIndex") page: Int,
        @Query("pageSize") pageSize: Int
    ): Observable<SalePointsDto>
}

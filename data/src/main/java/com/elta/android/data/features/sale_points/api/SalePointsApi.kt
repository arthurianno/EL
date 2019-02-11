package com.elta.android.data.features.sale_points.api

import com.elta.android.data.features.sale_points.dto.SalePointsDto
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface SalePointsApi {

    // TODO: change url
    @GET("api/salepoints/v1")
    @Suppress("ForbiddenComment")
    fun getSalePoints(
        @Query("lastTimestampSync") lastSync: Long?,
        @Query("pageIndex") page: Int,
        @Query("pageSize") pageSize: Int
    ): Observable<SalePointsDto>
}
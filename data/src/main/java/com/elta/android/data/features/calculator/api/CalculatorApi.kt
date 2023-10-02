package com.elta.android.data.features.calculator.api

import com.elta.android.data.features.calculator.model.ProductResponse
import com.elta.android.data.features.calculator.model.VerifiedProductResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path

interface CalculatorApi {

    @GET("/v1/products")
    fun getEventProducts(
        @Path("event_id") eventId: String
    ): Observable<List<ProductResponse>>

    @GET("api/diary/v1/products")
    fun getVerifiedProducts(): Observable<List<VerifiedProductResponse>>
}

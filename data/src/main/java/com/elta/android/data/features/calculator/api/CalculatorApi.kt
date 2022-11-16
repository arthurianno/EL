package com.elta.android.data.features.calculator.api

import com.elta.android.data.features.calculator.dto.ProductDto
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path

interface CalculatorApi {

    @GET("/v1/products")
    fun getEventProducts(
        @Path("event_id") eventId: String
    ): Observable<List<ProductDto>>
}

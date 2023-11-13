package com.elta.android.data.features.calculator.api

import com.elta.android.data.features.calculator.model.MetricServingUnitResponse
import com.elta.android.data.features.calculator.model.ProductItemResponse
import com.elta.android.data.features.calculator.model.ProductsResponse
import com.elta.android.data.features.calculator.model.StoredProductNetworkEntity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApi {

    @POST("api/diary/products/v2")
    fun addProduct(
        @Body storedProduct: StoredProductNetworkEntity
    ): Observable<ProductItemResponse>

    @GET("api/diary/products/v2")
    fun getProducts(
        @Query("customOnly") customOnly: Boolean,
        @Query("foodName") foodName: String?,
        @Query("pageIndex") pageIndex: Int,
        @Query("pageSize") pageSize: Int
    ): Single<ProductsResponse>

    @GET("api/diary/products/v2/{food_id}")
    fun getProduct(@Path("food_id") foodId: String): Observable<ProductItemResponse>

    @DELETE("api/diary/products/v2/{food_id}")
    fun removeProduct(@Path("food_id") foodId: String): Completable

    @GET("api/diary/products/v2/servings")
    fun getServingsProduct(): Observable<List<MetricServingUnitResponse>>

}

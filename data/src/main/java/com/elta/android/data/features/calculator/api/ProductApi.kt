package com.elta.android.data.features.calculator.api

import com.elta.android.data.features.calculator.model.MetricServingUnitResponse
import com.elta.android.data.features.calculator.model.ProductItemResponse
import com.elta.android.data.features.calculator.model.ProductsResponse
import com.elta.android.data.features.calculator.model.StoredProductNetworkEntity
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApi {

    @POST("api/diary/products/v2")
    suspend fun addProduct(
        @Body storedProduct: StoredProductNetworkEntity
    ): ProductItemResponse

    @GET("api/diary/products/v2")
    suspend fun getProducts(
        @Query("customOnly") customOnly: Boolean,
        @Query("foodName") foodName: String?,
        @Query("pageIndex") pageIndex: Int,
        @Query("pageSize") pageSize: Int
    ): ProductsResponse

    @GET("api/diary/products/v2/{food_id}")
    suspend fun getProduct(@Path("food_id") foodId: String): ProductItemResponse

    @DELETE("api/diary/products/v2/{food_id}")
    suspend fun removeProduct(@Path("food_id") foodId: String)

    @GET("api/diary/products/v2/servings")
    suspend fun getServingsProduct(): List<MetricServingUnitResponse>

}

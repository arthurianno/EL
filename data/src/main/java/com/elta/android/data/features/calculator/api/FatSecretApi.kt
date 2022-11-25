package com.elta.android.data.features.calculator.api

import com.elta.android.data.features.calculator.model.FoodBrandResponse
import com.elta.android.data.features.calculator.model.FoodGenericResponse
import com.elta.android.data.features.calculator.model.FoodsSearchResponse
import io.reactivex.Observable
import retrofit2.http.POST
import retrofit2.http.Query

private const val FORMAT_RESPONSE = "json"

interface FatSecretApi {

    @POST(".")
    fun getFoodGeneric(
        @Query("food_id") foodId: String,
        @Query("method") method: String = "food.get.v2",
        @Query("format") format: String = FORMAT_RESPONSE
    ): Observable<FoodGenericResponse>

    @POST(".")
    fun getFoodBrand(
        @Query("food_id") foodId: String,
        @Query("method") method: String = "food.get.v2",
        @Query("format") format: String = FORMAT_RESPONSE
    ): Observable<FoodBrandResponse>

    @POST(".")
    fun getFoods(
        @Query("search_expression") searchWord: String,
        @Query("method") method: String = "foods.search",
        @Query("format") format: String = FORMAT_RESPONSE
    ): Observable<FoodsSearchResponse>
}

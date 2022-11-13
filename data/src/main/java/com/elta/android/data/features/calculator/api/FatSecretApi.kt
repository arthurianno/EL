package com.elta.android.data.features.calculator.api

import com.elta.android.data.features.calculator.dto.FoodBrandDto
import com.elta.android.data.features.calculator.dto.FoodGenericDto
import com.elta.android.data.features.calculator.dto.FoodsSearchDto
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
    ): Observable<FoodGenericDto>

    @POST(".")
    fun getFoodBrand(
        @Query("food_id") foodId: String,
        @Query("method") method: String = "food.get.v2",
        @Query("format") format: String = FORMAT_RESPONSE
    ): Observable<FoodBrandDto>

    @POST(".")
    fun getFoods(
        @Query("search_expression") searchWord: String,
        @Query("method") method: String = "foods.search",
        @Query("format") format: String = FORMAT_RESPONSE
    ): Observable<FoodsSearchDto>
}

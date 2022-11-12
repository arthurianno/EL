package com.elta.android.data.features.calculator.dto

import com.google.gson.annotations.SerializedName

data class FoodsSearchDto(
    @SerializedName("foods") val foods: FoodSearchDto
) {
    data class FoodSearchDto(
        @SerializedName("food") val food: List<CompactFoodDto>?,
        @SerializedName("max_results") val maxResults: String,
        @SerializedName("page_number") val pageNumber: String,
        @SerializedName("total_results") val totalResults: String
    )
}

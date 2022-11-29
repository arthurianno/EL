package com.elta.android.data.features.calculator.model

import com.google.gson.annotations.SerializedName

data class FoodsSearchResponse(
    @SerializedName("foods") val foods: Foods
) {
    data class Foods(
        @SerializedName("food") val food: List<CompactFoodNetworkEntity>?,
        @SerializedName("max_results") val maxResults: String,
        @SerializedName("page_number") val pageNumber: String,
        @SerializedName("total_results") val totalResults: String
    )
}

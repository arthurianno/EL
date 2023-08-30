package com.elta.android.data.features.calculator.model

import com.google.gson.annotations.SerializedName

data class SearchFoodsResponse(
    @SerializedName("foods_search") val foodsSearch: FoodsSearch
) {
    data class FoodsSearch(
        @SerializedName("max_results") val maxResults: String,
        @SerializedName("page_number") val pageNumber: String,
        @SerializedName("results") val results: FoodResponse?,
        @SerializedName("total_results") val totalResults: String
    )
}

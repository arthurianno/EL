package com.elta.android.data.features.calculator.model


import com.elta.android.data.features.common.dto.MetaDto

data class ProductsResponse(
    val items: List<ProductItemResponse>,
    val meta: MetaDto,
)
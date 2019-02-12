package com.elta.android.domain.features.sale_points.interactor

const val MIN_QUERY_LENGTH = 3
fun isSearchInputValid(query: String): Boolean = query.length >= MIN_QUERY_LENGTH
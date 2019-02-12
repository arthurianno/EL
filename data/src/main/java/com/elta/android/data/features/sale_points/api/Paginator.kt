package com.elta.android.data.features.sale_points.api

fun <T> List<T>.getPage(page: Int, pageSize: Int): List<T> {
    val lastIndex = page * pageSize
    val startIndex = lastIndex - pageSize
    return subList(startIndex, if (lastIndex < size) lastIndex else size)
}

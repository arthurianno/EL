package com.elta.android.data.features.common

import com.elta.android.data.features.common.dto.MetaDto

fun <T> List<T>.getPage(page: Int, pageSize: Int): List<T> {
    val lastIndex = page * pageSize
    val startIndex = lastIndex - pageSize
    return subList(startIndex, if (lastIndex < size) lastIndex else size)
}

fun MetaDto.isTheLastPage(): Boolean = currentPage * pageSize >= totalItems

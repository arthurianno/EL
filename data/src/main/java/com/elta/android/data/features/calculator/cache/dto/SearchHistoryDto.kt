package com.elta.android.data.features.calculator.cache.dto

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class SearchHistoryDto(
    @Id(assignable = true) var id: Long,
    var word: String,
    val time: Long
)

package com.elta.android.data.features.calculator.cache.model

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class SearchHistoryDbEntity(
    @Id(assignable = true) var id: Long,
    var word: String,
    val time: Long
)

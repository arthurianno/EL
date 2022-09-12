package com.elta.android.data.features.sale_points.cache.dto

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.annotation.IndexType

@Entity
data class SalePointCacheDto(
    @Id(assignable = true) var id: Long,
    val secondaryId: String,
    val name: String,
    val type: String,
    val region: String,
    val city: String,
    val address: String,
    val phone: String?,
    val latitude: Double,
    val longitude: Double,
    val timeStamp: Int,
    val modifiedState: String,
    @Index(type = IndexType.DEFAULT)
    val fullAddress: String
)

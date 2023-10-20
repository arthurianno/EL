package com.elta.android.data.features.diary.events.cache.dto.v2

import com.elta.android.data.features.diary.events.cache.converter.MedicamentDtoConverter
import com.elta.android.data.features.diary.events.dto.v2.MedicamentDto
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class EventV2CachedDto(
    @Id(assignable = true) var id: Long,
    val secondaryId: String,
    val type: String,
    val additionTime: Long,
    val additionTimeString: String,
    val tagId: String?,
    val note: String?,
    val modificationTime: Long?,
    val products: String?,
    val temperature: Double?,
    val value: Double?,
    val name: String?,
    val kind: String?,
    val duration: Long?,
    val activityType: String?,
    val mealTag: String?,
    @Convert(converter = MedicamentDtoConverter::class, dbType = String::class)
    val medicament: MedicamentDto?,
    val state: String,
    val glucometerSerialNumber: String?
)

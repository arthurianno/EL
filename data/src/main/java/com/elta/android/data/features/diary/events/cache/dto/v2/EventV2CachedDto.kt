package com.elta.android.data.features.diary.events.cache.dto.v2

import com.elta.android.data.features.diary.events.cache.converter.GlucoseInputTypeDtoConverter
import com.elta.android.data.features.diary.events.cache.converter.InsulinMedicamentDtoConverter
import com.elta.android.data.features.diary.events.cache.converter.MedicamentDtoConverter
import com.elta.android.data.features.diary.events.dto.GlucoseInputTypeDto
import com.elta.android.data.features.diary.events.dto.v2.InsulinMedicamentDto
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
    @Convert(converter = GlucoseInputTypeDtoConverter::class, dbType = String::class)
    val glucoseInputType: GlucoseInputTypeDto?,

    @Convert(converter = InsulinMedicamentDtoConverter::class, dbType = String::class)
    val medicament: InsulinMedicamentDto?,
    //incorrect named field. Correct entity InsulinMedicament

    @Convert(converter = MedicamentDtoConverter::class, dbType = String::class)
    val medicamentDto: MedicamentDto?,
    val tabletsNumber: Double?,

    val state: String,
    val glucometerSerialNumber: String?,
    val isTimeInvalid: Boolean = false,
    val isTemperatureInvalid: Boolean = false
)

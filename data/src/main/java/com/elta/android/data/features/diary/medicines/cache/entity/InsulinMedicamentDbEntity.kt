package com.elta.android.data.features.diary.medicines.cache.entity

import com.elta.android.data.features.diary.medicines.cache.converter.InsulinTypeDbEntityConverter
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Uid

@Entity
@Uid(8999350013555102540L)
data class InsulinMedicamentDbEntity(
    @Id(assignable = true) var id: Long,
    val name: String,
    @Convert(converter = InsulinTypeDbEntityConverter::class, dbType = String::class)
    val insulinType: InsulinTypeDbEntity,
    val deleted: Boolean,
    val isOther: Boolean = false,
    val countryCode: String? = null,
    val sortOrder: Int = 0
)

package com.elta.android.data.features.diary.insulin.cache.medicines

import com.elta.android.data.features.diary.insulin.cache.converter.InsulinTypeDbEntityConverter
import com.elta.android.data.features.diary.insulin.cache.insulin.InsulinTypeDbEntity
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class MedicamentDbEntity(
    @Id(assignable = true) var id: Long,
    val name: String,
    @Convert(converter = InsulinTypeDbEntityConverter::class, dbType = String::class)
    val insulinType: InsulinTypeDbEntity,
    val deleted: Boolean
)

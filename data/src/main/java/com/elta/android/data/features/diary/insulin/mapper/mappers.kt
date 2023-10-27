package com.elta.android.data.features.diary.insulin.mapper

import com.elta.android.data.features.diary.insulin.cache.insulin.InsulinTypeDbEntity
import com.elta.android.data.features.diary.insulin.cache.medicines.MedicamentDbEntity
import com.elta.android.data.features.diary.insulin.cache.statistic.InsulinStatisticDbEntity
import com.elta.android.data.features.diary.insulin.dto.MedicinesNetworkResponse
import com.elta.android.domain.features.diary.events.model.Medicament
import com.elta.android.domain.features.diary.events.model.MedicamentInsulinType

fun MedicinesNetworkResponse.toDb(): Triple<List<MedicamentDbEntity>, List<InsulinTypeDbEntity>, InsulinStatisticDbEntity> {
    val list = insulinMedicamentsByType
        .values
        .flatten()

    val medicamentDb = list
        .map {
            MedicamentDbEntity(
                id = it.id.toLong(),
                name = it.name,
                insulinType = it.insulinType.toDb(),
                deleted = it.deleted
            )
        }
    val insulinTypeDb = list
        .filterNot { it.deleted }
        .map { it.insulinType.toDb() }
        .distinct()

    val statisticTypeDb = InsulinStatisticDbEntity(
        bolusInsulinTypes = bolusInsulinTypes,
        basalInsulinTypes = basalInsulinTypes
    )


    return Triple(medicamentDb, insulinTypeDb, statisticTypeDb)
}


fun MedicamentInsulinType.toDb(): InsulinTypeDbEntity = InsulinTypeDbEntity(
    id = id.toLong(),
    name = name,
    code = code
)

fun List<MedicamentDbEntity>.toDomainMedicines(): List<Medicament> = map { it.toDomain() }

private fun MedicamentDbEntity.toDomain() =
    Medicament(
        id = id.toInt(),
        name = name,
        insulinType = insulinType.toDomain(),
        deleted = deleted
    )

fun List<InsulinTypeDbEntity>.toDomain(): List<MedicamentInsulinType> = map { it.toDomain() }

private fun InsulinTypeDbEntity.toDomain(): MedicamentInsulinType = MedicamentInsulinType(
    id = id.toInt(),
    code = code,
    name = name
)

private fun MedicinesNetworkResponse.InsulinType.toDb(): InsulinTypeDbEntity =
    InsulinTypeDbEntity(
        code = code,
        id = id.toLong(),
        name = name,
    )


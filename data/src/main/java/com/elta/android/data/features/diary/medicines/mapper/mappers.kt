package com.elta.android.data.features.diary.medicines.mapper

import com.elta.android.data.features.diary.medicines.cache.entity.InsulinMedicamentDbEntity
import com.elta.android.data.features.diary.medicines.cache.entity.InsulinStatisticDbEntity
import com.elta.android.data.features.diary.medicines.cache.entity.InsulinTypeDbEntity
import com.elta.android.data.features.diary.medicines.cache.entity.MedicamentDBEntity
import com.elta.android.data.features.diary.medicines.dto.InsulinMedicamentsNetworkResponse
import com.elta.android.data.features.diary.medicines.dto.MedicamentNetworkResponse
import com.elta.android.domain.features.diary.medicines.model.InsulinMedicament
import com.elta.android.domain.features.diary.medicines.model.Medicament
import com.elta.android.domain.features.diary.medicines.model.MedicamentInsulinType

fun InsulinMedicamentsNetworkResponse.toDb(): Triple<List<InsulinMedicamentDbEntity>, List<InsulinTypeDbEntity>, InsulinStatisticDbEntity> {
    val list = insulinMedicamentsByType
        .values
        .flatten()

    val medicamentDb = list
        .map {
            InsulinMedicamentDbEntity(
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

fun List<InsulinMedicamentDbEntity>.toDomainMedicines(): List<InsulinMedicament> =
    map { it.toDomain() }

private fun InsulinMedicamentDbEntity.toDomain() =
    InsulinMedicament(
        id = id.toInt(),
        name = name,
        insulinType = insulinType.toDomain(),
        deleted = deleted
    )

fun List<InsulinTypeDbEntity>.toInsulinDomain(): List<MedicamentInsulinType> = map { it.toDomain() }

private fun InsulinTypeDbEntity.toDomain(): MedicamentInsulinType = MedicamentInsulinType(
    id = id.toInt(),
    code = code,
    name = name
)

fun MedicamentNetworkResponse.toDB(): MedicamentDBEntity = MedicamentDBEntity(
    id = id,
    name = name,
    other = other,
    deleted = deleted,
    touchedAt = touchedAt,
    lastUsed = null
)

fun Medicament.toDb(lastUsed: Long): MedicamentDBEntity = MedicamentDBEntity(
    id = id,
    name = name,
    other = isOther,
    deleted = isDeleted,
    touchedAt = touchedAt,
    lastUsed = lastUsed
)

fun List<MedicamentDBEntity>.toDomain(): List<Medicament> = map {
    Medicament(
        id = it.id,
        name = it.name,
        isOther = it.other,
        isDeleted = it.deleted,
        touchedAt = it.touchedAt
    )
}

private fun InsulinMedicamentsNetworkResponse.InsulinType.toDb(): InsulinTypeDbEntity =
    InsulinTypeDbEntity(
        code = code,
        id = id.toLong(),
        name = name,
    )


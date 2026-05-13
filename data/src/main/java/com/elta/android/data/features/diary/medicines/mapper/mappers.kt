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
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.Locale

fun InsulinMedicamentsNetworkResponse.toDb(
    countryCode: String = DEFAULT_INSULIN_COUNTRY_CODE
): Triple<List<InsulinMedicamentDbEntity>, List<InsulinTypeDbEntity>, InsulinStatisticDbEntity> {
    val normalizedCountryCode = countryCode.normalizedCountryCode()
    val list = insulinMedicamentsByType.values.flatten()

    val medicamentDb = insulinMedicamentsByType
        .values
        .flatMap { items ->
            items.mapIndexed { index, item ->
                InsulinMedicamentDbEntity(
                    id = item.id.toLong(),
                    name = item.name,
                    insulinType = item.insulinType.toDb(countryCode = normalizedCountryCode),
                    deleted = item.deleted,
                    isOther = item.isOther,
                    countryCode = normalizedCountryCode,
                    sortOrder = index
                )
            }
        }
    val insulinTypeDb = list
        .filterNot { it.deleted }
        .map { it.insulinType }
        .distinctBy { it.code }
        .mapIndexed { index, insulinType ->
            insulinType.toDb(
                countryCode = normalizedCountryCode,
                sortOrder = index
            )
        }

    val statisticTypeDb = InsulinStatisticDbEntity(
        bolusInsulinTypes = bolusInsulinTypes,
        basalInsulinTypes = basalInsulinTypes,
        countryCode = normalizedCountryCode
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
        deleted = deleted,
        isOther = isOther
    )

fun List<InsulinTypeDbEntity>.toInsulinDomain(): List<MedicamentInsulinType> = map { it.toDomain() }

private fun InsulinTypeDbEntity.toDomain(): MedicamentInsulinType = MedicamentInsulinType(
    id = id.toInt(),
    code = code,
    name = name
)

fun MedicamentNetworkResponse.toDB(countryCode: String, languageTag: String): MedicamentDBEntity = MedicamentDBEntity(
    id = medicamentCacheId(id, countryCode, languageTag),
    medicamentId = id,
    countryCode = countryCode.normalizedCountryCode(),
    languageTag = languageTag.normalizedLanguageTag(),
    name = name,
    other = other,
    deleted = deleted,
    touchedAt = touchedAt,
    lastUsed = null
)

fun Medicament.toDb(
    lastUsed: Long,
    countryCode: String,
    languageTag: String
): MedicamentDBEntity = MedicamentDBEntity(
    id = medicamentCacheId(id, countryCode, languageTag),
    medicamentId = id,
    countryCode = countryCode.normalizedCountryCode(),
    languageTag = languageTag.normalizedLanguageTag(),
    name = name,
    other = isOther,
    deleted = isDeleted,
    touchedAt = touchedAt,
    lastUsed = lastUsed
)

fun List<MedicamentDBEntity>.toDomain(): List<Medicament> = map {
    Medicament(
        id = it.medicamentId ?: it.id,
        name = it.name,
        isOther = it.other,
        isDeleted = it.deleted,
        touchedAt = it.touchedAt
    )
}

private fun InsulinMedicamentsNetworkResponse.InsulinType.toDb(
    countryCode: String,
    sortOrder: Int = 0
): InsulinTypeDbEntity =
    InsulinTypeDbEntity(
        code = code,
        id = id.toLong(),
        name = name,
        countryCode = countryCode,
        sortOrder = sortOrder
    )

fun medicamentCacheId(medicamentId: Long, countryCode: String, languageTag: String): Long {
    val key = "${countryCode.normalizedCountryCode()}:${languageTag.normalizedLanguageTag()}:$medicamentId"
    val digest = MessageDigest.getInstance("SHA-256").digest(key.toByteArray(Charsets.UTF_8))
    val value = ByteBuffer.wrap(digest, 0, Long.SIZE_BYTES).long and Long.MAX_VALUE
    return if (value == 0L) 1L else value
}

fun MedicamentDBEntity.matchesLocale(countryCode: String, languageTag: String): Boolean =
    this.countryCode?.normalizedCountryCode() == countryCode.normalizedCountryCode() &&
        this.languageTag?.normalizedLanguageTag() == languageTag.normalizedLanguageTag()

fun MedicamentDBEntity.isLegacyLocale(): Boolean =
    countryCode == null && languageTag == null && medicamentId == null

fun String.normalizedCountryCode(): String = trim().uppercase(Locale.ROOT)

fun String.normalizedLanguageTag(): String = trim().lowercase(Locale.ROOT)

private const val DEFAULT_INSULIN_COUNTRY_CODE = "RU"

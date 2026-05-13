package com.elta.android.data.features.diary.medicines.datasource.cache

import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.diary.medicines.cache.conditions.InsulinMedicamentConditions
import com.elta.android.data.features.diary.medicines.cache.entity.InsulinMedicamentDbEntity
import com.elta.android.data.features.diary.medicines.cache.entity.InsulinStatisticDbEntity
import com.elta.android.data.features.diary.medicines.cache.entity.InsulinTypeDbEntity
import com.elta.android.data.features.diary.medicines.mapper.normalizedCountryCode
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class InsulinMedicamentCacheDataSource @Inject constructor(
    private val insulinMedicamentCache: Cache<InsulinMedicamentDbEntity>,
    private val insulinTypeCache: Cache<InsulinTypeDbEntity>,
    private val statisticCache: Cache<InsulinStatisticDbEntity>
) : InsulinMedicamentCacheSource {

    override fun saveInsulinMedicaments(medicines: List<InsulinMedicamentDbEntity>): Completable {
        return Completable.fromCallable {
            insulinMedicamentCache.delete(CommonConditions.All)
            insulinMedicamentCache.add(medicines)
        }
    }

    override fun getInsulinMedicaments(countryCode: String): Observable<List<InsulinMedicamentDbEntity>> {
        return Observable.just(
            insulinMedicamentCache.getAll(CommonConditions.All)
                .filterMedicamentsByCountry(countryCode)
                .ordered()
        )
    }

    override fun getInsulinMedicaments(
        type: InsulinTypeDbEntity,
        countryCode: String
    ): Observable<List<InsulinMedicamentDbEntity>> {
        return Observable.just(
            insulinMedicamentCache.getAll(InsulinMedicamentConditions.ByInsulinType(type))
                .filterMedicamentsByCountry(countryCode)
                .ordered()
        )
    }

    override fun saveInsulinType(insulinTypes: List<InsulinTypeDbEntity>): Completable {
        return Completable.fromCallable {
            insulinTypeCache.delete(CommonConditions.All)
            insulinTypeCache.add(insulinTypes)
        }
    }

    override fun getInsulinTypes(countryCode: String): Observable<List<InsulinTypeDbEntity>> {
        return Observable.just(
            insulinTypeCache.getAll(CommonConditions.All)
                .filterTypesByCountry(countryCode)
                .sortedBy { it.sortOrder }
        )
    }

    override fun saveInsulinStatisticType(insulinStatisticDbEntity: InsulinStatisticDbEntity): Completable {
        return Completable.fromCallable {
            statisticCache.delete(CommonConditions.All)
            statisticCache.add(listOf(insulinStatisticDbEntity))
        }
    }

    override fun getInsulinStatisticType(countryCode: String): Observable<InsulinStatisticDbEntity> {
        return Observable.just(
            statisticCache.getAll(CommonConditions.All)
                .firstOrNull { it.matchesCountry(countryCode) }
                ?: InsulinStatisticDbEntity.empty(countryCode.normalizedCountryCode())
        )
    }

    override fun hasInsulinCache(countryCode: String): Single<Boolean> =
        Single.fromCallable {
            statisticCache.getAll(CommonConditions.All).any { it.matchesCountry(countryCode) } &&
                insulinMedicamentCache.getAll(CommonConditions.All).any { it.matchesCountry(countryCode) }
        }

    override fun clearInsulinCache(): Completable =
        Completable.fromCallable {
            insulinMedicamentCache.delete(CommonConditions.All)
            insulinTypeCache.delete(CommonConditions.All)
            statisticCache.delete(CommonConditions.All)
        }

    private fun List<InsulinMedicamentDbEntity>.filterMedicamentsByCountry(
        countryCode: String
    ): List<InsulinMedicamentDbEntity> =
        filter { it.matchesCountry(countryCode) }

    private fun List<InsulinTypeDbEntity>.filterTypesByCountry(countryCode: String): List<InsulinTypeDbEntity> =
        filter { it.matchesCountry(countryCode) }

    private fun List<InsulinMedicamentDbEntity>.ordered(): List<InsulinMedicamentDbEntity> =
        sortedWith(
            compareBy<InsulinMedicamentDbEntity> { it.insulinType.code }
                .thenBy { it.isOther }
                .thenBy { it.sortOrder }
        )

    private fun InsulinMedicamentDbEntity.matchesCountry(countryCode: String): Boolean =
        this.countryCode?.normalizedCountryCode() == countryCode.normalizedCountryCode()

    private fun InsulinTypeDbEntity.matchesCountry(countryCode: String): Boolean =
        this.countryCode?.normalizedCountryCode() == countryCode.normalizedCountryCode()

    private fun InsulinStatisticDbEntity.matchesCountry(countryCode: String): Boolean =
        this.countryCode?.normalizedCountryCode() == countryCode.normalizedCountryCode()
}

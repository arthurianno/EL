package com.elta.android.data.features.diary.medicines.repository

import com.elta.android.data.features.common.network.ApiCountryCodeResolver
import com.elta.android.data.features.common.network.CountryCodeProvider
import com.elta.android.data.features.diary.medicines.cache.entity.InsulinMedicamentDbEntity
import com.elta.android.data.features.diary.medicines.datasource.cache.InsulinMedicamentCacheSource
import com.elta.android.data.features.diary.medicines.datasource.remote.InsulinMedicamentRemoteDataSource
import com.elta.android.data.features.diary.medicines.mapper.toDb
import com.elta.android.data.features.diary.medicines.mapper.toInsulinDomain
import com.elta.android.data.features.diary.medicines.mapper.toDomainMedicines
import com.elta.android.domain.features.diary.medicines.model.InsulinMedicament
import com.elta.android.domain.features.diary.medicines.model.InsulinMedicamentStatistic
import com.elta.android.domain.features.diary.medicines.model.MedicamentInsulinType
import com.elta.android.domain.features.diary.medicines.repository.InsulinMedicamentRepository
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

class InsulinMedicamentDataRepository internal constructor(
    private val insulinMedicamentRemoteDataSource: InsulinMedicamentRemoteDataSource,
    private val insulinMedicamentCacheSource: InsulinMedicamentCacheSource,
    private val countryCodeProvider: CountryCodeProvider
) : InsulinMedicamentRepository {

    @Inject
    constructor(
        insulinMedicamentRemoteDataSource: InsulinMedicamentRemoteDataSource,
        insulinMedicamentCacheSource: InsulinMedicamentCacheSource,
        countryCodeResolver: ApiCountryCodeResolver
    ) : this(
        insulinMedicamentRemoteDataSource = insulinMedicamentRemoteDataSource,
        insulinMedicamentCacheSource = insulinMedicamentCacheSource,
        countryCodeProvider = countryCodeResolver
    )

    override fun getInsulinMedicaments(type: MedicamentInsulinType): Observable<List<InsulinMedicament>> {
        val countryCode = countryCodeProvider.countryCode()
        return when (type) {

            MedicamentInsulinType.allMedicament() -> ensureCacheForCountry(countryCode)
                .andThen(
                    Observable.defer {
                        insulinMedicamentCacheSource.getInsulinMedicaments(countryCode)
                    }
                )
                .map { list -> list.filterNot { medicament -> medicament.isOtherMedicament() } }

            else -> ensureCacheForCountry(countryCode)
                .andThen(
                    Observable.defer {
                        insulinMedicamentCacheSource.getInsulinMedicaments(type.toDb(), countryCode)
                    }
                )

        }
            .map {
                it.filterNot { medicament -> medicament.deleted }
                    .toDomainMedicines()
            }
    }

    override fun getInsulinTypes(): Observable<List<MedicamentInsulinType>> {
        val countryCode = countryCodeProvider.countryCode()
        return ensureCacheForCountry(countryCode)
            .andThen(
                Observable.defer {
                    insulinMedicamentCacheSource.getInsulinTypes(countryCode)
                }
            )
            .map { it.toInsulinDomain() }

    }

    override fun getBasalAndBolusTypes(): Observable<InsulinMedicamentStatistic> {
        val countryCode = countryCodeProvider.countryCode()
        return ensureCacheForCountry(countryCode)
            .andThen(
                Observable.defer {
                    Observable.combineLatest(
                        insulinMedicamentCacheSource.getInsulinStatisticType(countryCode),
                        insulinMedicamentCacheSource.getInsulinTypes(countryCode).map { it.toInsulinDomain() }
                    ) { statisticType, insulinTypes ->

                        val bolus = insulinTypes.filter { type -> type.code in statisticType.bolusInsulinTypes }
                        val basal = insulinTypes.filter { type -> type.code in statisticType.basalInsulinTypes }

                        InsulinMedicamentStatistic(
                            bolusInsulinTypes = bolus,
                            basalInsulinTypes = basal
                        )
                    }
                }
            )
    }

    override fun sync(): Completable =
        insulinMedicamentRemoteDataSource.getInsulinMedicines()
            .map { result ->
                result.response.toDb(countryCode = result.countryCode)
            }
            .flatMapCompletable { (medicines, insulinTypes, insulinStatistic) ->
                Completable.concat(
                    listOf(
                        insulinMedicamentCacheSource.saveInsulinMedicaments(medicines),
                        insulinMedicamentCacheSource.saveInsulinType(insulinTypes),
                        insulinMedicamentCacheSource.saveInsulinStatisticType(insulinStatistic)
                    )
                )
            }

    private fun ensureCacheForCountry(countryCode: String): Completable =
        insulinMedicamentCacheSource.hasInsulinCache(countryCode)
            .flatMapCompletable { hasCache ->
                if (hasCache) {
                    Completable.complete()
                } else {
                    insulinMedicamentCacheSource.clearInsulinCache()
                        .andThen(sync())
                }
            }
}

const val OTHER = "Другое"

private fun InsulinMedicamentDbEntity.isOtherMedicament(): Boolean =
    isOther || name == OTHER

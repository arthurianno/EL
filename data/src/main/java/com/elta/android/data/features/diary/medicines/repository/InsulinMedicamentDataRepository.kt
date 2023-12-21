package com.elta.android.data.features.diary.medicines.repository

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

class InsulinMedicamentDataRepository @Inject constructor(
    private val insulinMedicamentRemoteDataSource: InsulinMedicamentRemoteDataSource,
    private val insulinMedicamentCacheSource: InsulinMedicamentCacheSource,
) : InsulinMedicamentRepository {

    override fun getInsulinMedicaments(type: MedicamentInsulinType): Observable<List<InsulinMedicament>> {
        return when (type) {

            MedicamentInsulinType.allMedicament() -> insulinMedicamentCacheSource.getInsulinMedicaments()
                .map { list -> list.filterNot { medicament -> medicament.name == OTHER } }

            else -> insulinMedicamentCacheSource.getInsulinMedicaments(type.toDb())

        }
            .map {
                it.filterNot { medicament -> medicament.deleted }
                    .toDomainMedicines()
            }
    }

    override fun getInsulinTypes(): Observable<List<MedicamentInsulinType>> {
        return insulinMedicamentCacheSource.getInsulinTypes()
            .map { it.toInsulinDomain() }

    }

    override fun getBasalAndBolusTypes(): Observable<InsulinMedicamentStatistic> =
        Observable.combineLatest(
            insulinMedicamentCacheSource.getInsulinStatisticType(),
            insulinMedicamentCacheSource.getInsulinTypes().map { it.toInsulinDomain() }
        ) { statisticType, insulinTypes ->

            val bolus = insulinTypes.filter { type -> type.code in statisticType.bolusInsulinTypes }
            val basal = insulinTypes.filter { type -> type.code in statisticType.basalInsulinTypes }

            InsulinMedicamentStatistic(
                bolusInsulinTypes = bolus,
                basalInsulinTypes = basal
            )
        }

    override fun sync(): Completable =
        insulinMedicamentRemoteDataSource.getInsulinMedicines()
            .map { it.toDb() }
            .flatMapCompletable { (medicines, insulinTypes, insulinStatistic) ->
                Completable.concat(
                    listOf(
                        insulinMedicamentCacheSource.saveInsulinMedicaments(medicines),
                        insulinMedicamentCacheSource.saveInsulinType(insulinTypes),
                        insulinMedicamentCacheSource.saveInsulinStatisticType(insulinStatistic)
                    )
                )
            }

}

const val OTHER = "Другое"

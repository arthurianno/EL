package com.elta.android.data.features.diary.insulin.repository

import com.elta.android.data.features.diary.insulin.datasource.cache.MedicinesCacheSource
import com.elta.android.data.features.diary.insulin.datasource.remote.MedicinesRemoteDataSource
import com.elta.android.data.features.diary.insulin.mapper.toDb
import com.elta.android.data.features.diary.insulin.mapper.toDomain
import com.elta.android.data.features.diary.insulin.mapper.toDomainMedicines
import com.elta.android.domain.features.diary.events.model.Medicament
import com.elta.android.domain.features.diary.events.model.MedicamentInsulinStatistic
import com.elta.android.domain.features.diary.events.model.MedicamentInsulinType
import com.elta.android.domain.features.diary.insulin.MedicinesRepository
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

class MedicinesDataRepository @Inject constructor(
    private val medicinesRemoteDataSource: MedicinesRemoteDataSource,
    private val medicinesCacheSource: MedicinesCacheSource,
) : MedicinesRepository {

    override fun getMedicines(type: MedicamentInsulinType): Observable<List<Medicament>> {
        return when (type) {

            MedicamentInsulinType.allMedicament() -> medicinesCacheSource.getMedicines()
                .map { list -> list.filterNot { medicament -> medicament.name == OTHER } }

            else -> medicinesCacheSource.getMedicines(type.toDb())

        }
            .map {
                it.filterNot { medicament -> medicament.deleted }
                    .toDomainMedicines()
            }
    }

    override fun getInsulinTypes(): Observable<List<MedicamentInsulinType>> {
        return medicinesCacheSource.getInsulinTypes()
            .map { it.toDomain() }

    }

    override fun getBasalAndBolusTypes(): Observable<MedicamentInsulinStatistic> =
        Observable.combineLatest(
            medicinesCacheSource.getStatisticType(),
            medicinesCacheSource.getInsulinTypes().map { it.toDomain() }
        ) { statisticType, insulinTypes ->

            val bolus = insulinTypes.filter { type -> type.code in statisticType.bolusInsulinTypes }
            val basal = insulinTypes.filter { type -> type.code in statisticType.basalInsulinTypes }

            MedicamentInsulinStatistic(
                bolusInsulinTypes = bolus,
                basalInsulinTypes = basal
            )
        }

    override fun sync(): Completable =
        medicinesRemoteDataSource.getInsulinMedicines()
            .map { it.toDb() }
            .flatMapCompletable { (medicines, insulinTypes, insulinStatistic) ->
                Completable.concat(
                    listOf(
                        medicinesCacheSource.saveMedicines(medicines),
                        medicinesCacheSource.saveInsulinType(insulinTypes),
                        medicinesCacheSource.saveStatisticType(insulinStatistic)
                    )
                )
            }

}

const val OTHER = "Другое"

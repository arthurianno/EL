package com.elta.android.data.features.diary.insulin.datasource.cache

import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.diary.insulin.cache.insulin.InsulinTypeDbEntity
import com.elta.android.data.features.diary.insulin.cache.medicines.MedicamentDbEntity
import com.elta.android.data.features.diary.insulin.cache.medicines.MedicinesConditions
import com.elta.android.data.features.diary.insulin.cache.statistic.InsulinStatisticDbEntity
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

class MedicinesCacheDataSource @Inject constructor(
    private val medicamentCache: Cache<MedicamentDbEntity>,
    private val insulinTypeCache: Cache<InsulinTypeDbEntity>,
    private val statisticCache: Cache<InsulinStatisticDbEntity>
) : MedicinesCacheSource {

    override fun saveMedicines(medicines: List<MedicamentDbEntity>): Completable {
        return Completable.fromCallable {
            medicamentCache.delete(CommonConditions.All)
            medicamentCache.add(medicines)
        }
    }

    override fun getMedicines(): Observable<List<MedicamentDbEntity>> {
        return Observable.just(
            medicamentCache.getAll(CommonConditions.All)
        )
    }

    override fun getMedicines(type: InsulinTypeDbEntity): Observable<List<MedicamentDbEntity>> {
        return Observable.just(
            medicamentCache.getAll(MedicinesConditions.ByInsulinType(type))
        )
    }

    override fun saveInsulinType(insulinTypes: List<InsulinTypeDbEntity>): Completable {
        return Completable.fromCallable {
            insulinTypeCache.delete(CommonConditions.All)
            insulinTypeCache.add(insulinTypes)
        }
    }

    override fun getInsulinTypes(): Observable<List<InsulinTypeDbEntity>> {
        return Observable.just(
            insulinTypeCache.getAll(CommonConditions.All)
        )
    }

    override fun saveStatisticType(insulinStatisticDbEntity: InsulinStatisticDbEntity): Completable {
        return Completable.fromCallable {
            statisticCache.delete(CommonConditions.All)
            statisticCache.add(listOf(insulinStatisticDbEntity))
        }
    }

    override fun getStatisticType(): Observable<InsulinStatisticDbEntity> {
        return Observable.just(
            statisticCache.getAll(CommonConditions.All).first()
        )
    }
}

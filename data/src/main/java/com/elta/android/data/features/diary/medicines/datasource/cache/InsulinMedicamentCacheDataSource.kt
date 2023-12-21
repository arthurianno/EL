package com.elta.android.data.features.diary.medicines.datasource.cache

import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.diary.medicines.cache.conditions.InsulinMedicamentConditions
import com.elta.android.data.features.diary.medicines.cache.entity.InsulinMedicamentDbEntity
import com.elta.android.data.features.diary.medicines.cache.entity.InsulinStatisticDbEntity
import com.elta.android.data.features.diary.medicines.cache.entity.InsulinTypeDbEntity
import io.reactivex.Completable
import io.reactivex.Observable
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

    override fun getInsulinMedicaments(): Observable<List<InsulinMedicamentDbEntity>> {
        return Observable.just(
            insulinMedicamentCache.getAll(CommonConditions.All)
        )
    }

    override fun getInsulinMedicaments(type: InsulinTypeDbEntity): Observable<List<InsulinMedicamentDbEntity>> {
        return Observable.just(
            insulinMedicamentCache.getAll(InsulinMedicamentConditions.ByInsulinType(type))
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

    override fun saveInsulinStatisticType(insulinStatisticDbEntity: InsulinStatisticDbEntity): Completable {
        return Completable.fromCallable {
            statisticCache.delete(CommonConditions.All)
            statisticCache.add(listOf(insulinStatisticDbEntity))
        }
    }

    override fun getInsulinStatisticType(): Observable<InsulinStatisticDbEntity> {
        return Observable.just(
            statisticCache.getAll(CommonConditions.All).first()
        )
    }
}

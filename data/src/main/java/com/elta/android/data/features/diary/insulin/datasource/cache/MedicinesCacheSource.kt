package com.elta.android.data.features.diary.insulin.datasource.cache

import com.elta.android.data.features.diary.insulin.cache.statistic.InsulinStatisticDbEntity
import com.elta.android.data.features.diary.insulin.cache.insulin.InsulinTypeDbEntity
import com.elta.android.data.features.diary.insulin.cache.medicines.MedicamentDbEntity
import io.reactivex.Completable
import io.reactivex.Observable

interface MedicinesCacheSource {

    fun saveMedicines(medicines: List<MedicamentDbEntity>): Completable
    fun getMedicines(): Observable<List<MedicamentDbEntity>>
    fun getMedicines(type: InsulinTypeDbEntity): Observable<List<MedicamentDbEntity>>

    fun saveInsulinType(insulinTypes: List<InsulinTypeDbEntity>): Completable
    fun getInsulinTypes(): Observable<List<InsulinTypeDbEntity>>

    fun saveStatisticType(insulinStatisticDbEntity: InsulinStatisticDbEntity): Completable
    fun getStatisticType(): Observable<InsulinStatisticDbEntity>
}

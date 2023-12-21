package com.elta.android.data.features.diary.medicines.datasource.cache

import com.elta.android.data.features.diary.medicines.cache.entity.InsulinMedicamentDbEntity
import com.elta.android.data.features.diary.medicines.cache.entity.InsulinStatisticDbEntity
import com.elta.android.data.features.diary.medicines.cache.entity.InsulinTypeDbEntity
import io.reactivex.Completable
import io.reactivex.Observable

interface InsulinMedicamentCacheSource {

    fun saveInsulinMedicaments(medicines: List<InsulinMedicamentDbEntity>): Completable
    fun getInsulinMedicaments(): Observable<List<InsulinMedicamentDbEntity>>
    fun getInsulinMedicaments(type: InsulinTypeDbEntity): Observable<List<InsulinMedicamentDbEntity>>

    fun saveInsulinType(insulinTypes: List<InsulinTypeDbEntity>): Completable
    fun getInsulinTypes(): Observable<List<InsulinTypeDbEntity>>

    fun saveInsulinStatisticType(insulinStatisticDbEntity: InsulinStatisticDbEntity): Completable
    fun getInsulinStatisticType(): Observable<InsulinStatisticDbEntity>
}

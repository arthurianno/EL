package com.elta.android.data.features.diary.medicines.datasource.cache

import com.elta.android.data.features.diary.medicines.cache.entity.InsulinMedicamentDbEntity
import com.elta.android.data.features.diary.medicines.cache.entity.InsulinStatisticDbEntity
import com.elta.android.data.features.diary.medicines.cache.entity.InsulinTypeDbEntity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface InsulinMedicamentCacheSource {

    fun saveInsulinMedicaments(medicines: List<InsulinMedicamentDbEntity>): Completable
    fun getInsulinMedicaments(countryCode: String): Observable<List<InsulinMedicamentDbEntity>>
    fun getInsulinMedicaments(type: InsulinTypeDbEntity, countryCode: String): Observable<List<InsulinMedicamentDbEntity>>

    fun saveInsulinType(insulinTypes: List<InsulinTypeDbEntity>): Completable
    fun getInsulinTypes(countryCode: String): Observable<List<InsulinTypeDbEntity>>

    fun saveInsulinStatisticType(insulinStatisticDbEntity: InsulinStatisticDbEntity): Completable
    fun getInsulinStatisticType(countryCode: String): Observable<InsulinStatisticDbEntity>

    fun hasInsulinCache(countryCode: String): Single<Boolean>
    fun clearInsulinCache(): Completable
}

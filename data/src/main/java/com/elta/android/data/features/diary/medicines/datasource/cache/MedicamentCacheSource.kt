package com.elta.android.data.features.diary.medicines.datasource.cache

import com.elta.android.data.features.diary.medicines.cache.entity.MedicamentDBEntity
import io.reactivex.Completable
import kotlinx.coroutines.flow.Flow

interface MedicamentCacheSource {

    fun getRecentlySearches(): Flow<List<MedicamentDBEntity>>
    fun saveRecentlySearches(medicament: MedicamentDBEntity)

    fun saveMedicaments(medicaments: List<MedicamentDBEntity>): Completable
    fun getMedicaments(): Flow<List<MedicamentDBEntity>>

}

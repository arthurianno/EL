package com.elta.android.data.features.diary.medicines.datasource.cache

import com.elta.android.data.features.diary.medicines.cache.entity.MedicamentDBEntity
import io.reactivex.Completable
import kotlinx.coroutines.flow.Flow

interface MedicamentCacheSource {

    fun getRecentlySearches(countryCode: String, languageTag: String): Flow<List<MedicamentDBEntity>>
    fun saveRecentlySearches(medicament: MedicamentDBEntity)

    fun saveMedicaments(
        medicaments: List<MedicamentDBEntity>,
        countryCode: String,
        languageTag: String
    ): Completable

    fun getMedicaments(countryCode: String, languageTag: String): Flow<List<MedicamentDBEntity>>

}

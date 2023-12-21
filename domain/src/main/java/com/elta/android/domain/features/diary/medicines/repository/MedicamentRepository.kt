package com.elta.android.domain.features.diary.medicines.repository

import com.elta.android.common.repository.BaseRepository
import com.elta.android.domain.features.diary.medicines.model.Medicament
import io.reactivex.Completable
import kotlinx.coroutines.flow.Flow

interface MedicamentRepository : BaseRepository {

    fun getRecentlyUsed():  Flow<List<Pair<Medicament, String?>>>
    fun getRecentlySearches(): Flow<List<Medicament>>
    fun saveRecentlySearches(medicament: Medicament)
    fun getMedicaments(): Flow<List<Medicament>>
    fun sync(): Completable
}

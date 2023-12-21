package com.elta.android.data.features.diary.medicines.datasource.cache

import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.common.cache.Condition
import com.elta.android.data.features.diary.medicines.cache.conditions.MedicamentConditions
import com.elta.android.data.features.diary.medicines.cache.entity.MedicamentDBEntity
import io.reactivex.Completable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MedicamentCacheDataSource @Inject constructor(
    private val medicamentCache: Cache<MedicamentDBEntity>
) : MedicamentCacheSource {

    override fun getRecentlySearches(): Flow<List<MedicamentDBEntity>> {
        return flow {
            val list = medicamentCache.getAll(MedicamentConditions.LastUsed)
            emit(list)
        }
    }

    override fun saveRecentlySearches(medicament: MedicamentDBEntity) {
        medicamentCache.update(listOf(medicament))
    }

    override fun saveMedicaments(medicaments: List<MedicamentDBEntity>): Completable {

        return Completable.fromCallable {
            val (deletedList, actualList) = medicaments.partition { it.deleted }
            val deleteListIds = deletedList.map { it.id }
            val lastSearchedList = medicamentCache.getAll(MedicamentConditions.LastUsed)

            val saves = (lastSearchedList + actualList).distinctBy { it.id }

            medicamentCache.delete(CommonConditions.ByIds(deleteListIds))
            medicamentCache.update(saves)
        }
    }

    override fun getMedicaments(): Flow<List<MedicamentDBEntity>> =
        flow {
            val list = medicamentCache.getAll(CommonConditions.All)
            emit(list)
        }
}

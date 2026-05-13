package com.elta.android.data.features.diary.medicines.datasource.cache

import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.diary.medicines.cache.entity.MedicamentDBEntity
import com.elta.android.data.features.diary.medicines.mapper.isLegacyLocale
import com.elta.android.data.features.diary.medicines.mapper.matchesLocale
import com.elta.android.data.features.diary.medicines.mapper.medicamentCacheId
import io.reactivex.Completable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MedicamentCacheDataSource @Inject constructor(
    private val medicamentCache: Cache<MedicamentDBEntity>
) : MedicamentCacheSource {

    override fun getRecentlySearches(countryCode: String, languageTag: String): Flow<List<MedicamentDBEntity>> {
        return flow {
            val list = medicamentCache.getAll(CommonConditions.All)
                .filterForLocale(countryCode, languageTag)
                .filter { it.lastUsed != null }
                .sortedByDescending { it.lastUsed }
                .take(MAX_RECENTLY_USED)
            emit(list)
        }
    }

    override fun saveRecentlySearches(medicament: MedicamentDBEntity) {
        medicamentCache.update(listOf(medicament))
    }

    override fun saveMedicaments(
        medicaments: List<MedicamentDBEntity>,
        countryCode: String,
        languageTag: String
    ): Completable {

        return Completable.fromCallable {
            val (deletedList, actualList) = medicaments.partition { it.deleted }
            val deleteListIds = deletedList.map {
                medicamentCacheId(it.medicamentId ?: it.id, countryCode, languageTag)
            }
            val lastSearchedList = medicamentCache.getAll(CommonConditions.All)
                .filterForLocale(countryCode, languageTag)
                .filter { it.lastUsed != null }

            val deletedIds = deleteListIds.toSet()
            val lastUsedByMedicamentId = lastSearchedList
                .filterNot { it.id in deletedIds }
                .associateBy { it.medicamentId ?: it.id }
            val actualIds = actualList.map { it.medicamentId ?: it.id }.toSet()
            val actualWithLastUsed = actualList.map { actual ->
                val lastUsed = lastUsedByMedicamentId[actual.medicamentId ?: actual.id]?.lastUsed
                if (lastUsed == null) actual else actual.copy(lastUsed = lastUsed)
            }
            val unmatchedLastSearched = lastUsedByMedicamentId
                .filterKeys { it !in actualIds }
                .values

            val saves = actualWithLastUsed + unmatchedLastSearched

            medicamentCache.delete(CommonConditions.ByIds(deleteListIds))
            medicamentCache.update(saves)
        }
    }

    override fun getMedicaments(countryCode: String, languageTag: String): Flow<List<MedicamentDBEntity>> =
        flow {
            val list = medicamentCache.getAll(CommonConditions.All)
                .filterForLocale(countryCode, languageTag)
            emit(list)
        }

    private fun List<MedicamentDBEntity>.filterForLocale(
        countryCode: String,
        languageTag: String
    ): List<MedicamentDBEntity> {
        val currentLocaleItems = filter { it.matchesLocale(countryCode, languageTag) }
        return currentLocaleItems.ifEmpty {
            if (countryCode.equals(DEFAULT_LEGACY_COUNTRY, ignoreCase = true) &&
                languageTag.equals(DEFAULT_LEGACY_LANGUAGE, ignoreCase = true)
            ) {
                filter(MedicamentDBEntity::isLegacyLocale)
            } else {
                emptyList()
            }
        }
    }
}

private const val DEFAULT_LEGACY_COUNTRY = "RU"
private const val DEFAULT_LEGACY_LANGUAGE = "ru"
private const val MAX_RECENTLY_USED = 5

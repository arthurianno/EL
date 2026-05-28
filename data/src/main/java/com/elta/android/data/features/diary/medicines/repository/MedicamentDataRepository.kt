package com.elta.android.data.features.diary.medicines.repository

import com.elta.android.common.di.qualifires.Cache
import com.elta.android.data.features.common.network.ApiCountryCodeResolver
import com.elta.android.data.features.common.network.ApiLocaleResolver
import com.elta.android.data.features.diary.events.datasource.cache.EventsCacheDataSource
import com.elta.android.data.features.diary.events.mapper.toDomain
import com.elta.android.data.features.diary.medicines.datasource.cache.MedicamentCacheSource
import com.elta.android.data.features.diary.medicines.datasource.remote.MedicamentRemoteDataSource
import com.elta.android.data.features.diary.medicines.mapper.toDB
import com.elta.android.data.features.diary.medicines.mapper.toDb
import com.elta.android.data.features.diary.medicines.mapper.toDomain
import com.elta.android.domain.features.diary.medicines.model.Medicament
import com.elta.android.domain.features.diary.medicines.repository.MedicamentRepository
import io.reactivex.Completable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.rx2.asFlow
import java.util.Calendar
import javax.inject.Inject

class MedicamentDataRepository @Inject constructor(
    private val medicamentRemoteDataSource: MedicamentRemoteDataSource,
    private val medicamentCacheSource: MedicamentCacheSource,
    @Cache private val eventsCacheSource: EventsCacheDataSource,
    private val countryCodeResolver: ApiCountryCodeResolver,
    override val dispatcher: CoroutineDispatcher
) : MedicamentRepository {

    override fun getRecentlyUsed(): Flow<List<Pair<Medicament, String?>>> {

        return eventsCacheSource.getEvents()
            .map { list ->
                list
                    .sortedByDescending { it.additionTime }
                    .map { eventDto -> eventDto.data.medicament?.toDomain() to eventDto.data.name
                    }
                    .filter { (medicament, name) ->
                        medicament != null
                    }
                    .map {
                        it.first!! to it.second
                    }
                    .distinctBy { (medicament, otherName) ->
                        medicament.id.toString() + otherName
                    }
                    .take(MAX_ELEMENT)
            }
            .asFlow()
            .flowOn(dispatcher)
    }

    override fun getRecentlySearches(): Flow<List<Medicament>> {
        val countryCode = countryCodeResolver.countryCode()
        val languageTag = ApiLocaleResolver.languageTag()
        return medicamentCacheSource.getRecentlySearches(countryCode, languageTag)
            .map { it.toDomain() }
            .flowOn(dispatcher)
    }

    override fun saveRecentlySearches(medicament: Medicament) {
        val medicamentDb = medicament.toDb(
            lastUsed = Calendar.getInstance().timeInMillis,
            countryCode = countryCodeResolver.countryCode(),
            languageTag = ApiLocaleResolver.languageTag()
        )
        medicamentCacheSource.saveRecentlySearches(medicamentDb)
    }

    override fun getMedicaments(): Flow<List<Medicament>> {
        val countryCode = countryCodeResolver.countryCode()
        val languageTag = ApiLocaleResolver.languageTag()
        return medicamentCacheSource.getMedicaments(countryCode, languageTag)
            .map { list ->
                list.toDomain().sortedWith(
                    compareBy<Medicament> { it.isOther }
                        .thenBy { it.name.lowercase() }
                )
            }
            .flowOn(dispatcher)
    }


    override fun sync(): Completable =
        medicamentRemoteDataSource.syncMedicaments()
            .map { result ->
                result to result.medicaments.map {
                    it.toDB(
                        countryCode = result.countryCode,
                        languageTag = result.languageTag
                    )
                }
            }
            .flatMapCompletable { (result, list) ->
                medicamentCacheSource.saveMedicaments(
                    medicaments = list,
                    countryCode = result.countryCode,
                    languageTag = result.languageTag
                )
            }

}

private const val MAX_ELEMENT = 5

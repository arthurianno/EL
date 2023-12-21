package com.elta.android.data.features.diary.medicines.repository

import com.elta.android.common.di.qualifires.Cache
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
        return medicamentCacheSource.getRecentlySearches()
            .map { it.toDomain() }
            .flowOn(dispatcher)
    }

    override fun saveRecentlySearches(medicament: Medicament) {
        val medicamentDb = medicament.toDb(lastUsed = Calendar.getInstance().timeInMillis)
        medicamentCacheSource.saveRecentlySearches(medicamentDb)
    }

    override fun getMedicaments(): Flow<List<Medicament>> {
        return medicamentCacheSource.getMedicaments()
            .map { it.toDomain() }
            .flowOn(dispatcher)
    }


    override fun sync(): Completable =
        medicamentRemoteDataSource.syncMedicaments()
            .map { list -> list.map { it.toDB() } }
            .flatMapCompletable { list ->
                medicamentCacheSource.saveMedicaments(list)
            }

}

private const val MAX_ELEMENT = 5

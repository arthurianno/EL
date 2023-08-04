package com.elta.android.data.features.diary.insulin.repository

import com.elta.android.data.features.diary.insulin.datasource.cache.DrugsCacheSource
import com.elta.android.data.features.diary.insulin.datasource.remote.DrugsRemoteSource
import com.elta.android.data.features.diary.insulin.mapper.DrugToCacheMapper
import com.elta.android.data.features.diary.insulin.mapper.toDrug
import com.elta.android.domain.features.diary.events.model.Drug
import com.elta.android.domain.features.diary.events.model.InsulinType
import com.elta.android.domain.features.diary.insulin.DrugNameRepository
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

class DrugNameDataRepository @Inject constructor(
    private val toCacheMapper: DrugToCacheMapper,
    private val remoteSource: DrugsRemoteSource,
    private val cacheSource: DrugsCacheSource
) : DrugNameRepository {
    override fun getDrugNames(type: InsulinType): Observable<List<String>> {
        val drugsCache = if (type == InsulinType.ALL) {
            cacheSource.getAll()
        } else {
            cacheSource.getDrugNames(type)
        }

        return drugsCache
            .flatMap {
                if (it.isEmpty()) {
                    sync().andThen(cacheSource.getDrugNames(type))
                } else {
                    Observable.just(it)
                }
            }
            .map { drugs ->
                drugs.map { it.name }
            }
    }

    override fun getAll(): Observable<List<Drug>> {
        return cacheSource.getAll()
                .map { it.toDrug() }
    }

    override fun sync(): Completable =
        Completable
            .fromCallable {
                InsulinType.values()
                    .filter { insulinType -> insulinType != InsulinType.ALL }
                    .map { insulinType ->
                    remoteSource.getDrugNames(insulinType)
                        .subscribe { cacheSource.saveDrugs(toCacheMapper.mapFromObjects(it)) }
                }
            }
}

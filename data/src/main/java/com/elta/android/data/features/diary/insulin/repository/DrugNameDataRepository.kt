package com.elta.android.data.features.diary.insulin.repository

import com.elta.android.common.di.qualifires.Cache
import com.elta.android.common.di.qualifires.Remote
import com.elta.android.data.features.diary.insulin.datasource.DrugsDataSource
import com.elta.android.data.features.diary.insulin.mapper.DrugToCacheMapper
import com.elta.android.domain.features.diary.events.model.InsulinType
import com.elta.android.domain.features.diary.insulin.DrugNameRepository
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

class DrugNameDataRepository @Inject constructor(
    private val toCacheMapper: DrugToCacheMapper,
    @Remote private val remoteSource: DrugsDataSource,
    @Cache private val cacheSource: DrugsDataSource
) : DrugNameRepository {
    override fun getDrugNames(type: InsulinType): Observable<List<String>> =
        cacheSource.getDrugNames(type)
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

    override fun sync(): Completable =
        Completable
            .fromCallable {
                InsulinType.values().map { insulinType ->
                    remoteSource.getDrugNames(insulinType)
                        .subscribe { cacheSource.saveDrugs(toCacheMapper.mapFromObjects(it)) }
                }
            }
}

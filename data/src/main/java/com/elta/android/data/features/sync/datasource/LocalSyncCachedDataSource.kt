package com.elta.android.data.features.sync.datasource

import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.sync.cache.LocalSyncChangesConditions
import com.elta.android.data.features.sync.cache.dto.LocalSyncCachedDto
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class LocalSyncCachedDataSource @Inject constructor(
    private val cached: Cache<LocalSyncCachedDto>
) : LocalSyncDataSource {

    override fun hasByClassAndId(id: String, clazz: String): Single<Boolean> = Single.fromCallable {
        cached.contains(LocalSyncChangesConditions.ByClassNameAndId(id, clazz))
    }

    override fun getSingleById(id: String, clazz: String): Single<LocalSyncCachedDto> =
        Single.fromCallable {
            cached.get(LocalSyncChangesConditions.ByClassNameAndId(id, clazz))
        }

    override fun getAll(): Observable<List<LocalSyncCachedDto>> = Observable.fromCallable {
        cached.getAll(CommonConditions.All)
    }

    override fun getAllByClass(clazz: String): Observable<List<LocalSyncCachedDto>> = Observable.fromCallable {
        cached.getAll(LocalSyncChangesConditions.ByClassName(clazz))
    }

    override fun getSingle(clazz: String): Single<LocalSyncCachedDto> =
        Single.fromCallable {
            cached.get(LocalSyncChangesConditions.ByClassName(clazz))
        }

    override fun add(dto: LocalSyncCachedDto): Completable = add(listOf(dto))

    override fun add(list: List<LocalSyncCachedDto>): Completable = Completable.fromCallable {
        cached.add(list)
    }

    override fun update(dto: LocalSyncCachedDto): Completable = Completable.fromCallable {
        cached.update(listOf(dto))
    }

    override fun remove(id: String, clazz: String): Completable = Completable.fromCallable {
        cached.delete(LocalSyncChangesConditions.ByClassNameAndId(id, clazz))
    }

    override fun clear(clazz: String): Completable = Completable.fromCallable {
        cached.delete(LocalSyncChangesConditions.ByClassName(clazz))
    }

    override fun clearAll() = Completable.fromCallable {
        cached.delete(CommonConditions.All)
    }
}
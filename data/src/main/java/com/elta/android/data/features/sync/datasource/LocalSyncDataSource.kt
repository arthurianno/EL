package com.elta.android.data.features.sync.datasource

import com.elta.android.data.features.sync.cache.dto.LocalSyncCachedDto
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface LocalSyncDataSource {

    fun getAll(): Observable<List<LocalSyncCachedDto>>

    fun getAllByClass(clazz: String): Observable<List<LocalSyncCachedDto>>

    fun hasByClassAndId(id: String, clazz: String): Single<Boolean>

    fun getSingleById(id: String, clazz: String): Single<LocalSyncCachedDto>

    fun getSingle(clazz: String): Single<LocalSyncCachedDto>

    fun update(dto: LocalSyncCachedDto): Completable

    fun add(list: List<LocalSyncCachedDto>): Completable

    fun add(dto: LocalSyncCachedDto): Completable

    fun remove(id: String, clazz: String): Completable

    fun clear(clazz: String): Completable

    fun clearAll(): Completable
}
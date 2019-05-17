@file:Suppress("UNCHECKED_CAST")

package com.elta.android.data.features.sync.manger

import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.sync.cache.dto.LocalSyncCachedDto
import com.elta.android.data.features.sync.configuration.LocalSyncConfig
import com.elta.android.data.features.sync.configuration.LocalSyncMapper
import com.elta.android.data.features.sync.datasource.LocalSyncDataSource
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class LocalSyncManger @Inject constructor(
    val dataSource: LocalSyncDataSource,
    val config: LocalSyncConfig
) {

    inline fun <reified T : Any> saveAsCreated(entity: T) =
        Observable.just(T::class.java)
            .map { config.map[it] }
            .flatMap {
                if (StateDto.CREATED in it.supportedState) Observable.just(it)
                else Observable.error(makeError(T::class.java.simpleName, StateDto.CREATED))
            }
            .map { (it.mapper as LocalSyncMapper<T>).mapToCreate(entity) }
            .flatMapCompletable {
                dataSource.add(it)
            }

    inline fun <reified T : Any> saveAsCreated(list: List<T>) = Observable.just(T::class.java)
        .map { config.map[it] }
        .flatMap {
            if (StateDto.UPDATED in it.supportedState) Observable.just(it)
            else Observable.error(makeError(T::class.java.simpleName, StateDto.CREATED))
        }
        .map { config -> list.map { (config.mapper as LocalSyncMapper<T>).mapToCreate(it) } }
        .flatMapCompletable { dataSource.add(it) }

    inline fun <reified T : Any> saveAsUpdated(entity: T) = Observable.just(T::class.java)
        .map { config.map[it] }
        .flatMap {
            if (StateDto.DELETED in it.supportedState) Observable.just(it)
            else Observable.error(makeError(T::class.java.simpleName, StateDto.CREATED))
        }
        .map { (it.mapper as LocalSyncMapper<T>).mapToUpdate(entity) }
        .flatMapCompletable { dto ->
            dataSource.hasByClassAndId(dto.id, dto.className)
                .flatMapCompletable { contains ->
                    when (contains) {
                        true -> dataSource.getSingleById(dto.id, dto.className)
                            .map {
                                when (it.state == StateDto.CREATED) {
                                    true -> dto.copy(state = StateDto.CREATED)
                                    else -> dto
                                }
                            }.flatMapCompletable {
                                dataSource.update(it)
                            }
                        else -> dataSource.add(dto)
                    }
                }
        }

    inline fun <reified T : Any> saveAsDeleted(entity: T) = Observable.just(T::class.java)
        .map { config.map[it] }
        .filter { StateDto.DELETED in it.supportedState }
        .map { (it.mapper as LocalSyncMapper<T>).mapToDelete(entity) }
        .flatMapCompletable { dto ->
            dataSource.hasByClassAndId(dto.id, dto.className)
                .flatMapCompletable { contains ->
                    when (contains) {
                        true -> dataSource.remove(dto.id, dto.className)
                        else -> dataSource.add(dto)
                    }
                }
        }

    inline fun <reified T : Any> setAllSynced() = dataSource.clear(T::class.java.simpleName)

    fun setSynced(dto: LocalSyncCachedDto) = dataSource.remove(dto.id, dto.className)

    inline fun <reified T : Any> getNotSynced(): Observable<List<LocalSyncCachedDto>> =
        dataSource.getAllByClass(T::class.java.simpleName)

    inline fun <reified T : Any> getSingleNotSynced(): Single<LocalSyncCachedDto> =
        dataSource.getSingle(T::class.java.simpleName)


    fun makeError(className: String, stateDto: StateDto) =
        UnsupportedOperationException("State = $stateDto is not available for class = $className")
}
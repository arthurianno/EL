@file:Suppress("UNCHECKED_CAST")

package com.elta.android.data.features.sync.manger

import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.sync.cache.dto.LocalSyncCachedDto
import com.elta.android.data.features.sync.configuration.ClassConfiguration
import com.elta.android.data.features.sync.configuration.LocalSyncConfig
import com.elta.android.data.features.sync.datasource.LocalSyncDataSource
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class LocalSyncManager @Inject constructor(
    val dataSource: LocalSyncDataSource,
    val config: LocalSyncConfig
) {

    inline fun <reified T : Any> saveAsCreated(entity: T) =
        Completable.fromCallable {
            val config = config.map[T::class.java] as? ClassConfiguration<T>
            config?.let { nonNullConfig ->
                if (StateDto.CREATED !in nonNullConfig.supportedState)
                    throw makeError(T::class.java.simpleName, StateDto.CREATED)
                dataSource.add(listOf(nonNullConfig.mapper.mapToCreate(entity)))
            }
        }

    inline fun <reified T : Any> saveAsCreated(list: List<T>) = Completable.fromCallable {
        val config = config.map[T::class.java] as? ClassConfiguration<T>
        config?.let { nonNullConfig ->
            if (StateDto.CREATED !in nonNullConfig.supportedState)
                throw makeError(T::class.java.simpleName, StateDto.CREATED)
            dataSource.add(list.map { nonNullConfig.mapper.mapToCreate(it) })
        }
    }

    inline fun <reified T : Any> saveAsUpdated(entity: T) = Completable.fromCallable {
        val config = config.map[T::class.java] as? ClassConfiguration<T>
        config?.let { nonNullConfig ->
            if (StateDto.UPDATED !in nonNullConfig.supportedState)
                throw makeError(T::class.java.simpleName, StateDto.UPDATED)
            val dto = nonNullConfig.mapper.mapToUpdate(entity)
            when (dataSource.hasByClassAndId(dto.secondaryId, dto.className)) {
                true -> {
                    val previous = dataSource.getSingleById(dto.secondaryId, dto.className)
                    if (previous?.state == StateDto.CREATED) {
                        dataSource.update(dto.copy(state = StateDto.CREATED))
                    } else {
                        dataSource.update(dto)
                    }
                }
                else -> dataSource.add(listOf(dto))
            }
        }
    }

    inline fun <reified T : Any> saveAsDeleted(entity: T) = Completable.fromCallable {
        val config = config.map[T::class.java] as? ClassConfiguration<T>
        config?.let { nonNullConfig ->
            if (StateDto.DELETED !in nonNullConfig.supportedState)
                throw makeError(T::class.java.simpleName, StateDto.DELETED)
            val dto = nonNullConfig.mapper.mapToDelete(entity)
            when (dataSource.hasByClassAndId(dto.secondaryId, dto.className)) {
                true -> dataSource.remove(dto.secondaryId, dto.className)
                else -> dataSource.add(listOf(dto))
            }
        }
    }

    inline fun <reified T : Any> setAllSynced() = Completable.fromCallable {
        dataSource.clear(T::class.java.simpleName)
    }

    fun setSynced(dto: LocalSyncCachedDto) = Completable.fromCallable {
        dataSource.remove(dto.secondaryId, dto.className)
    }

    inline fun <reified T : Any> getNotSynced(): Observable<List<LocalSyncCachedDto>> =
        Observable.fromCallable { dataSource.getAllByClass(T::class.java.simpleName) }

    inline fun <reified T : Any> getSingleNotSynced(): Single<LocalSyncCachedDto> =
        Single.fromCallable { dataSource.getSingle(T::class.java.simpleName) }

    fun makeError(className: String, stateDto: StateDto) =
        UnsupportedOperationException("State = $stateDto is not available for class = $className")
}
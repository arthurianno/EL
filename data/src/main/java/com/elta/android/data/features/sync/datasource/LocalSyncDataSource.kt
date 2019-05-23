package com.elta.android.data.features.sync.datasource

import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.sync.cache.dto.LocalSyncCachedDto

@Suppress("ComplexInterface", "TooManyFunctions")
interface LocalSyncDataSource {

    fun getAll(): List<LocalSyncCachedDto>

    fun getAllByClass(clazz: String): List<LocalSyncCachedDto>

    fun hasByClassAndId(id: String, clazz: String): Boolean

    fun hasByClass(clazz: String): Boolean

    fun getSingleById(id: String, clazz: String): LocalSyncCachedDto?

    fun getSingle(clazz: String): LocalSyncCachedDto?

    fun update(dto: LocalSyncCachedDto)

    fun add(list: List<LocalSyncCachedDto>)

    fun remove(id: String, clazz: String)

    fun remove(clazz: String, state: StateDto)

    fun clear(clazz: String)

    fun clearAll()
}
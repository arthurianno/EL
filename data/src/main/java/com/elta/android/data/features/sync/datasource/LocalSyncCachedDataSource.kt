package com.elta.android.data.features.sync.datasource

import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.sync.cache.LocalSyncChangesConditions
import com.elta.android.data.features.sync.cache.dto.LocalSyncCachedDto
import javax.inject.Inject

class LocalSyncCachedDataSource @Inject constructor(
    private val cached: Cache<LocalSyncCachedDto>
) : LocalSyncDataSource {

    override fun hasByClassAndId(id: String, clazz: String) =
        cached.contains(LocalSyncChangesConditions.ByClassNameAndId(id, clazz))

    override fun hasByClass(clazz: String) =
        cached.contains(LocalSyncChangesConditions.ByClassName(clazz))

    override fun getSingleById(id: String, clazz: String): LocalSyncCachedDto? =
        cached.get(LocalSyncChangesConditions.ByClassNameAndId(id, clazz))

    override fun getAll() = cached.getAll(CommonConditions.All)

    override fun getAllByClass(clazz: String): List<LocalSyncCachedDto> =
        cached.getAll(LocalSyncChangesConditions.ByClassName(clazz))

    override fun getSingle(clazz: String): LocalSyncCachedDto? =
        cached.get(LocalSyncChangesConditions.ByClassName(clazz))

    override fun add(list: List<LocalSyncCachedDto>) = cached.add(list)

    override fun update(dto: LocalSyncCachedDto) = cached.update(listOf(dto))

    override fun remove(id: String, clazz: String) =
        cached.delete(LocalSyncChangesConditions.ByClassNameAndId(id, clazz))

    override fun remove(clazz: String, state: StateDto) =
        cached.delete(LocalSyncChangesConditions.ByClassAndState(clazz, state))

    override fun clear(clazz: String) = cached.delete(LocalSyncChangesConditions.ByClassName(clazz))

    override fun clearAll() = cached.delete(CommonConditions.All)
}

package com.elta.android.data.features.common.cache

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.dto.DataWithStateDto
import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.common.storage.UserHolder

fun <T : DataWithStateDto, R> updateCache(datas: List<T>, cache: Cache<R>, mapper: Mapper<in T, R>) {
    val created = mutableListOf<T>()
    val updated = mutableListOf<T>()
    val deleted = mutableListOf<T>()
    datas.forEach { data ->
        when (data.state) {
            StateDto.CREATED -> created.add(data)
            StateDto.UPDATED -> updated.add(data)
            StateDto.DELETED -> deleted.add(data)
        }
    }

    cache.add(mapper.mapFromObjects(created))
    cache.update(mapper.mapFromObjects(updated))
    cache.delete(CommonConditions.ByIds(deleted.map { it.id.hashCode().toLong() }))
}

fun <T> UserHolder.doInUserExists(action: (user: Long) -> T): T {
    currentUser?.let { return action.invoke(it) } ?: throw AccessDeniedError
}
package com.elta.android.data.features.common.cache

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.dto.DataWithStateDto
import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.common.storage.UserHolder

fun <T : DataWithStateDto, R> updateCache(datas: List<T>, cache: Cache<R>, mapper: Mapper<in T, R>) {
    val states = mutableMapOf<StateDto, MutableList<DataWithStateDto>>()
    datas.forEach { data ->
        var list = states[data.state]
        if (list == null) {
            list = mutableListOf()
            states[data.state] = list
        }
        list.add(data)
    }
    states.forEach { entry ->
        when (entry.key) {
            StateDto.CREATED -> cache.add(mapper.mapFromObjects(entry.value as Collection<T>))
            StateDto.DELETED -> cache.delete(CommonConditions.ByIds(entry.value.map { it.id.hashCode().toLong() }))
            StateDto.UPDATED -> cache.update(mapper.mapFromObjects(entry.value as Collection<T>))
        }
    }
}

fun <T> UserHolder.doInUserExists(action: (user: Long) -> T): T {
    currentUser?.let { return action.invoke(it) } ?: throw AccessDeniedError
}
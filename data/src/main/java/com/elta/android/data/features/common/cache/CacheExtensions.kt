package com.elta.android.data.features.common.cache

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.dto.DataWithStateDto
import com.elta.android.data.features.common.dto.StateDto

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
        val forCache = mapper.mapFromObjects(entry.value as Collection<T>)
        when (entry.key) {
            StateDto.CREATED -> cache.add(forCache)
            StateDto.DELETED -> cache.delete(forCache)
            StateDto.UPDATED -> cache.update(forCache)
        }
    }
}
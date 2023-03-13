package com.elta.android.data.features.common.cache

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.dto.DataWithStateDto
import com.elta.android.data.features.common.dto.StateDto

@Deprecated("Метод использует устаревший маппер")
fun <T : DataWithStateDto, R> updateCache(
    datas: List<T>,
    cache: Cache<R>,
    mapper: Mapper<in T, R>
) {
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

fun <T : DataWithStateDto, R> updateCache(items: List<T>, cache: Cache<R>, mapper: T.() -> R) =
    with(cache) {
        add(
            items.filter { it.state == StateDto.CREATED }
                .map(mapper)
        )
        update(
            items.filter { it.state == StateDto.UPDATED }
                .map(mapper)
        )
        delete(
            CommonConditions.ByIds(
                items.filter { it.state == StateDto.DELETED }
                    .map { it.id.hashCode().toLong() }
            )
        )
    }

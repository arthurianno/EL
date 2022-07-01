package com.elta.android.common.mapper

interface Mapper<S, D> {

    fun mapFromObject(source: S): D

    fun mapFromObjects(sources: Collection<S>) = sources.map { mapFromObject(it) }
}

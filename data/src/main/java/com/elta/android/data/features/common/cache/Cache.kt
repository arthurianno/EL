package com.elta.android.data.features.common.cache

interface Cache<T> {

    fun add(objects: List<T>)

    fun update(objects: List<T>)

    fun delete(condition: Condition)

    fun get(condition: Condition): List<T>
}
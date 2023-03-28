package com.elta.android.presentation.core.compose // ktlint-disable filename

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

const val TAG = "ELTA_TAG"

fun <T, K> Flow<T>.groupBy(getKey: (T) -> K): Flow<Pair<K, List<T>>> = flow {
    map {
        val storage = mutableMapOf<K, MutableList<T>>()
        storage.getOrPut(getKey(it)) { mutableListOf() } += it
        storage.forEach { (k, ts) -> emit(k to ts) }
    }
}

inline fun <T, R> Flow<T>.mapDistinct(crossinline transform: suspend (T) -> R): Flow<R> =
    map(transform).distinctUntilChanged()

fun <T> Flow<T>.log(additionalText: String? = null) = map {
    Log.d(TAG, "Flow log: $it - ${additionalText.orEmpty()}")
    it
}

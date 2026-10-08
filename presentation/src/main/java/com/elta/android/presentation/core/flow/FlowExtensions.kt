package com.elta.android.presentation.core.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

inline fun <T, R> Flow<T>.mapDistinct(crossinline transform: suspend (T) -> R): Flow<R> =
    map(transform).distinctUntilChanged()

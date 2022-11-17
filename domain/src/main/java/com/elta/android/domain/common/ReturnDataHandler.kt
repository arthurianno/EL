package com.elta.android.domain.common

import kotlinx.coroutines.flow.Flow

interface ReturnDataHandler<T> {
    fun asFlow(): Flow<T>
    fun onNext(data: T): Flow<Unit>
}

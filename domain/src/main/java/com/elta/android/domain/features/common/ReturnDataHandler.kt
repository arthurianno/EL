package com.elta.android.domain.features.common

import kotlinx.coroutines.flow.Flow

interface ReturnDataHandler<T> {
    fun asFlow(): Flow<T>
    fun returnResult(data: T): Flow<Unit>
}

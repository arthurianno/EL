package com.elta.android.domain.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow

interface ReturnDataHandler<T> {
    fun resultAsFlow(): Flow<T>
    fun onNext(data: T): Flow<Unit>

    companion object {
        fun <T> resultObject(): ReturnDataHandler<T> = object : ReturnDataHandler<T> {
            private var result: MutableSharedFlow<T> = MutableSharedFlow(extraBufferCapacity = 1)
            override fun resultAsFlow(): Flow<T> = result

            override fun onNext(data: T): Flow<Unit> = flow {
                emit(result.emit(data))
            }
        }
    }
}

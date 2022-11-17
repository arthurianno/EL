package com.elta.android.domain.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow

class BaseReturnDataHandler<T> : ReturnDataHandler<T> {
    private var _resultListener: MutableStateFlow<T?> = MutableStateFlow(null)
    override fun asFlow(): Flow<T> =
        _resultListener.filterNotNull()

    override fun onNext(data: T): Flow<Unit> = flow {
        emit(_resultListener.emit(data))
    }
}

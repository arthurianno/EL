package com.elta.android.domain.features.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow

open class BaseFragmentResultManager<T> : FragmentResultManager<T> {
    private var _fragmentResult: MutableStateFlow<T?> = MutableStateFlow(null)
    override val fragmentResult: Flow<T> =
        _fragmentResult.filterNotNull()

    override fun sendFragmentResult(data: T): Flow<Unit> = flow {
        emit(_fragmentResult.emit(data))
    }
}

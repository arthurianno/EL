package com.elta.android.domain.features.common

import kotlinx.coroutines.flow.Flow

interface FragmentResultManager<T> {
    val fragmentResult: Flow<T>
    fun sendFragmentResult(data: T): Flow<Unit>
}

package com.elta.android.common.repository

import kotlinx.coroutines.flow.Flow

interface FragmentResultManager<T> {
    val fragmentResult: Flow<T>
    fun sendFragmentResult(data: T): Flow<Unit>
}

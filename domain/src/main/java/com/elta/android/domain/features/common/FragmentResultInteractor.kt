package com.elta.android.domain.features.common

import kotlinx.coroutines.flow.Flow

interface FragmentResultInteractor<T> {
    fun dataFlow(): Flow<T>
    fun sendData(data: T): Flow<Unit>
}

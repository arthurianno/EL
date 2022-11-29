package com.elta.android.common.repository

import kotlinx.coroutines.CoroutineDispatcher

interface BaseRepository {
    val dispatcher: CoroutineDispatcher
}

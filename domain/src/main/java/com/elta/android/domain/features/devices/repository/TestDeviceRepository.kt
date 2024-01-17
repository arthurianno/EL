package com.elta.android.domain.features.devices.repository

import kotlinx.coroutines.flow.Flow

interface TestDeviceRepository {

    fun scan(): Flow<List<String>>

    suspend fun testConnect()

}

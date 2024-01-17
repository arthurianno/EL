package com.elta.android.data.features.devices.repository

import com.elta.android.common.repository.BaseRepository
import com.elta.android.data.features.devices.glucometer.refactor.Manager
import com.elta.android.domain.features.devices.repository.TestDeviceRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class TestDeviceDataRepository @Inject constructor(
    private val manager: Manager,
    override val dispatcher: CoroutineDispatcher
) : TestDeviceRepository, BaseRepository {
    override fun scan(): Flow<List<String>> {
        return manager.scan().map {
            Timber.tag(TAG).d("ScanResults: $it")
            it.map { result -> result.device.address }
        }
    }

    override suspend fun testConnect() {
        val result = manager.connectDevice(MAC, PIN)
        Timber.tag(TAG).d("Connect success: $result")
    }
}

private const val MAC = "E0:F9:1F:5D:E0:50"
private const val PIN = "409"

private const val TAG = "NORDIC_TEST"

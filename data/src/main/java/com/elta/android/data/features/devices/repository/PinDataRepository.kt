package com.elta.android.data.features.devices.repository

import com.elta.android.common.repository.BaseRepository
import com.elta.android.data.features.devices.glucometer.storage.GlucometerPinStorage
import com.elta.android.domain.features.devices.repository.PinRepository
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PinDataRepository @Inject constructor(
    private val pinStorage: GlucometerPinStorage,
    override val dispatcher: CoroutineDispatcher
) : PinRepository, BaseRepository {

    override fun getPin(address: String): String? {
        return pinStorage.getPin(address)
    }

    override fun savePin(address: String, pin: String) {
        pinStorage.setPin(address, pin)
    }

    override fun clearPin(address: String) {
        pinStorage.setPin(address, "") //TODO: можно сразу clear сделать в box
    }
}
package com.elta.android.domain.features.devices.interactor

import com.elta.android.common.errors.GlucometerPinNotFoundInternaly
import com.elta.android.domain.features.devices.repository.DeviceRepository
import com.elta.android.domain.features.devices.repository.PinRepository
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import javax.inject.Inject

class LocateGlucometerUserCase @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val pinRepository: PinRepository,
) {
    operator fun invoke(address: String): Flow<Unit> = flow {

        val pin = pinRepository.getPin(address)
        if (pin == null) {
            throw GlucometerPinNotFoundInternaly
        }

        deviceRepository.connectDevice(address, pin)
        try {
            while (currentCoroutineContext().isActive) {
                emit(Unit)
                deviceRepository.locateGlucometer()
                delay(LOCATE_GLUCOMETER_DELAY)
            }
        } finally {
            deviceRepository.disconnect()
        }
    }
}

private const val LOCATE_GLUCOMETER_DELAY = 8000L
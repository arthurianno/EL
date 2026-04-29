package com.elta.android.domain.features.devices.interactor

import com.elta.android.common.errors.GlucometerPinNotFoundInternaly
import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
import com.elta.android.common.utils.hideMac
import com.elta.android.domain.features.devices.COMMAND_TIMEOUT
import com.elta.android.domain.features.devices.checkBluetoothAvailabilityAndPermissions
import com.elta.android.domain.features.devices.connectWithTimeout
import com.elta.android.domain.features.devices.repository.BluetoothStateRepository
import com.elta.android.domain.features.devices.repository.DeviceRepository
import com.elta.android.domain.features.devices.repository.PinRepository
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeout
import java.util.concurrent.TimeoutException
import javax.inject.Inject

class LocateGlucometerUserCase @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val pinRepository: PinRepository,
    private val bluetoothStateRepository: BluetoothStateRepository,
    private val crashlyticsReport: CrashlyticsReport
) {
    operator fun invoke(address: String): Flow<Unit> = flow {
        bluetoothStateRepository.checkBluetoothAvailabilityAndPermissions(
            isLocationNeeded = true,
            crashlyticsReport = crashlyticsReport
        )
        crashlyticsReport.log("start locating device with address ${address.hideMac()}")
        val pin = pinRepository.getPin(address)
        if (pin == null) {
            crashlyticsReport.writeException(GlucometerPinNotFoundInternaly)
            throw GlucometerPinNotFoundInternaly
        }

        deviceRepository.connectWithTimeout(address, pin, crashlyticsReport)
        try {
            while (currentCoroutineContext().isActive) {
                emit(Unit)
                try {
                    withTimeout(COMMAND_TIMEOUT) {
                        crashlyticsReport.log("Looking for a device (anti-loss)")
                        deviceRepository.locateGlucometer()
                    }
                } catch (e: TimeoutCancellationException) {
                    val error = TimeoutException("Could not find device (anti-loss) ${address.hideMac()}")
                    crashlyticsReport.writeException(error)
                    throw error
                }


                delay(LOCATE_GLUCOMETER_DELAY)
            }
        } finally {
            crashlyticsReport.log("The procedure for disconnecting the connection with the device has begun")
            deviceRepository.disconnect()
        }
    }
}

private const val LOCATE_GLUCOMETER_DELAY = 8000L
package com.elta.android.domain.features.devices.interactor

import com.elta.android.common.errors.GlucometerPinNotFoundInternaly
import com.elta.android.domain.features.devices.checkBluetoothAvailabilityAndPermissions
import com.elta.android.domain.features.devices.connectWithTimeout
import com.elta.android.domain.features.devices.repository.BluetoothStateRepository
import com.elta.android.domain.features.devices.repository.DeviceRepository
import com.elta.android.domain.features.devices.repository.PinRepository
import com.elta.android.domain.features.devices.repository.UpdateRepository
import com.elta.android.domain.features.firmware.model.FirmwareFile
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx2.rxObservable
import java.math.BigInteger
import javax.inject.Inject
import kotlin.coroutines.EmptyCoroutineContext

class UpdateDeviceFirmwareUseCase @Inject constructor(
    private val updateRepository: UpdateRepository,
    private val pinRepository: PinRepository,
    private val deviceRepository: DeviceRepository,
    private val bluetoothStateRepository: BluetoothStateRepository,
    schedulers: SchedulersFacade
) : ObservableWithTimerUseCase<String, UpdateDeviceFirmwareUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Observable<String> {
        bluetoothStateRepository.checkBluetoothAvailabilityAndPermissions()

        return rxObservable(EmptyCoroutineContext + Dispatchers.Unconfined) {
            val p = checkNotNull(params)

            val address = p.address

            val pin = pinRepository.getPin(address)
            if (pin == null) {
                //В логи
                throw GlucometerPinNotFoundInternaly
            }

            deviceRepository.connectWithTimeout(address, pin)

            try {
                resetAndLaunchTimer(this)
                deviceRepository.turnOnDfuMode()
            } finally {
                deviceRepository.disconnect()
                cancelTimer()
            }

            val dfuAddress = address.toDfuAddress()

            deviceRepository.connectWithTimeout(dfuAddress, pin, true)

            try {
                updateRepository.updateFirmware(dfuAddress, p.file)
            } finally {
                deviceRepository.disconnect()
            }

        }
    }

    data class Params(
        val address: String,
        val file: FirmwareFile
    )

    @Suppress("MagicNumber")
    private fun String.toDfuAddress(): String {
        val tokens = this.split(":")
        val token = tokens.last()
        val hex = BigInteger(token, 16)
        val new = hex.plus(BigInteger.ONE).toString(16).padStart(2, '0').takeLast(2)

        return tokens.joinToString(
            separator = ":",
            limit = tokens.size - 1,
            postfix = new,
            truncated = ""
        ).uppercase()
    }
}

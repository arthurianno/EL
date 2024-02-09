package com.elta.android.domain.features.devices.interactor

import com.elta.android.common.errors.GlucometerPinNotFoundInternaly
import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
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
    private val crashlyticsReport: CrashlyticsReport,
    schedulers: SchedulersFacade
) : ObservableWithTimerUseCase<String, UpdateDeviceFirmwareUseCase.Params>(
    schedulers,
    crashlyticsReport
) {

    override fun buildUseCaseObservable(params: Params?): Observable<String> {
        return rxObservable(EmptyCoroutineContext + Dispatchers.Unconfined) {
            crashlyticsReport.log("starting firmware update for device: ${params?.address}")
            crashlyticsReport.log("checking params")
            val p = checkNotNull(params)

            val address = p.address

            bluetoothStateRepository.checkBluetoothAvailabilityAndPermissions(crashlyticsReport)

            crashlyticsReport.log("checking pin")
            val pin = pinRepository.getPin(address)
            if (pin == null) {
                crashlyticsReport.writeException(GlucometerPinNotFoundInternaly)
                throw GlucometerPinNotFoundInternaly
            }

            crashlyticsReport.log("start connection with device $address with timeout")
            deviceRepository.connectWithTimeout(address, pin, false, crashlyticsReport)

            try {
                resetAndLaunchTimer(this)
                deviceRepository.turnOnDfuMode()
            } finally {
                crashlyticsReport.log("disconnect from device and cancelling timer")
                deviceRepository.disconnect()
                cancelTimer()
            }

            crashlyticsReport.log("creating dfu address")
            val dfuAddress = address.toDfuAddress()

            crashlyticsReport.log("connecting to device in dfu mode $address with timeout")
            deviceRepository.connectWithTimeout(dfuAddress, pin, true, crashlyticsReport)

            crashlyticsReport.log("start updating device: $address firmware with file ${p.file.path}")
            try {
                updateRepository.updateFirmware(dfuAddress, p.file)
            } catch (e: Exception) {
                crashlyticsReport.writeException(e)
                throw e
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

package com.elta.android.domain.features.devices.interactor

import com.elta.android.common.errors.GlucometerPinNotFoundInternaly
import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
import com.elta.android.domain.features.appsettings.AppSettingsRepository
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

class UpdateFirmwareWithDfuModeUseCase @Inject constructor(
    private val updateRepository: UpdateRepository,
    private val pinRepository: PinRepository,
    private val deviceRepository: DeviceRepository,
    private val bluetoothStateRepository: BluetoothStateRepository,
    private val appSettingsRepository: AppSettingsRepository,
    private val crashlyticsReport: CrashlyticsReport,
    schedulers: SchedulersFacade
) : ObservableWithTimerUseCase<String, UpdateFirmwareWithDfuModeUseCase.Params>(
    schedulers,
    crashlyticsReport
) {

    override fun buildUseCaseObservable(params: Params?): Observable<String> {
        return rxObservable(EmptyCoroutineContext + Dispatchers.Unconfined) {
            crashlyticsReport.log("Firmware update started for device: ${params?.address}")
            crashlyticsReport.log("Checking parameters for nullability")
            val p = checkNotNull(params)

            val address = p.address

            bluetoothStateRepository.checkBluetoothAvailabilityAndPermissions(
                crashlyticsReport = crashlyticsReport,
                isLocationNeeded = appSettingsRepository.isLocationNeeded
            )

            crashlyticsReport.log("Checking pin")
            val pin = pinRepository.getPin(address)
            if (pin == null) {
                crashlyticsReport.writeException(GlucometerPinNotFoundInternaly)
                throw GlucometerPinNotFoundInternaly
            }

            deviceRepository.connectWithTimeout(address, pin, crashlyticsReport)

            try {
                resetAndLaunchTimer(this)
                deviceRepository.turnOnDfuMode()
            } finally {
                crashlyticsReport.log("Disconnect from device and cancelling timer")
                deviceRepository.disconnect()
                cancelTimer()
            }

            crashlyticsReport.log("Creating dfu address")
            val dfuAddress = address.toDfuAddress()

            crashlyticsReport.log("Start updating device: $dfuAddress firmware with file ${p.file.path}")
            try {
                updateRepository.updateFirmwareWithDfuMode(dfuAddress, p.file)
            } catch (e: Exception) {
                crashlyticsReport.writeException(e)
                throw e
            }
        }
    }

    /**
     * Функция перевода mac-адреса глюкометра в адрес для BootMode. Берётся последний октет и повышается на 1.
     * Пример: адрес 00:11:22:33:FF:00 станет 00:11:22:33:FF:01.
     */
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

    data class Params(
        val address: String,
        val file: FirmwareFile
    )
}

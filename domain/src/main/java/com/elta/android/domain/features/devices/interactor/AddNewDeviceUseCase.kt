package com.elta.android.domain.features.devices.interactor

import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
import com.elta.android.common.utils.hideMac
import com.elta.android.domain.features.devices.checkBluetoothAvailabilityAndPermissions
import com.elta.android.domain.features.devices.connectWithTimeout
import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.repository.BluetoothStateRepository
import com.elta.android.domain.features.devices.repository.DeviceInfoRepository
import com.elta.android.domain.features.devices.repository.DeviceRepository
import com.elta.android.domain.features.devices.repository.PinRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx2.rxCompletable
import javax.inject.Inject
import kotlin.coroutines.EmptyCoroutineContext

class AddNewDeviceUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val pinRepository: PinRepository,
    private val deviceInfoRepository: DeviceInfoRepository,
    private val bluetoothStateRepository: BluetoothStateRepository,
    private val crashlyticsReport: CrashlyticsReport,
    schedulers: SchedulersFacade
) : CompletableUseCase<AddNewDeviceUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Completable {
        return rxCompletable(EmptyCoroutineContext + Dispatchers.Unconfined) {
            addDevice(params)
        }
    }

    private suspend fun addDevice(params: Params?) {
        crashlyticsReport.log("Started adding a new device with address: ${params?.device?.address?.hideMac()}")
        try {
            bluetoothStateRepository.checkBluetoothAvailabilityAndPermissions(crashlyticsReport)

            val (device, pincode) = requireNotNull(params) {
                val errorString = "Parameters for a new device cannot be null"
                crashlyticsReport.writeException(RuntimeException(errorString))
                errorString
            }
            val address = device.address

            deviceRepository.connectWithTimeout(address, pincode, crashlyticsReport)

            crashlyticsReport.log("Saving pin")
            pinRepository.savePin(address, pincode)

            crashlyticsReport.log("Started receiving data for the main device")
            val primaryDevice = deviceInfoRepository.getPrimaryDeviceWithLastEvent()?.first
            crashlyticsReport.log("Main device data received")

            if (primaryDevice == null) {
                crashlyticsReport.log("Main device data received is null")
                deviceInfoRepository.putDevice(glucometer = device, isPrimary = true)
            } else if (!primaryDevice.address.equals(address, true)) {
                crashlyticsReport.log("Main device is ${device.name}")
                deviceInfoRepository.putDevice(device, isPrimary = false)
            }
        } finally {
            crashlyticsReport.log("The procedure for disconnecting the connection with the device has begun")
            deviceRepository.disconnect()
        }
    }

    data class Params(val device: Glucometer, val pinCode: String)
}

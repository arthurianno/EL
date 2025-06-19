package com.elta.android.domain.features.devices.interactor

import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
import com.elta.android.common.utils.hideMac
import com.elta.android.domain.features.devices.checkBluetoothAvailabilityAndPermissions
import com.elta.android.domain.features.devices.connectWithTimeout
import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.devices.repository.BluetoothStateRepositoryVariantA
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

// fixme Variant A : improved_enabling_location

class AddNewDeviceUseCaseVariantA @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val pinRepository: PinRepository,
    private val deviceInfoRepository: DeviceInfoRepository,
    private val bluetoothStateRepository: BluetoothStateRepositoryVariantA,
    private val crashlyticsReport: CrashlyticsReport,
    schedulers: SchedulersFacade
) : CompletableUseCase<AddNewDeviceUseCaseVariantA.Params>(schedulers) {

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
            val deviceData = deviceInfoRepository.getPrimaryDeviceWithLastEvent()
            val primaryDevice = deviceData?.first
            crashlyticsReport.log("Main device data received")

            if (primaryDevice == null) {
                crashlyticsReport.log("Main device data received is null")
                deviceInfoRepository.putDevice(glucometer = device, isPrimary = true)
            } else if (!primaryDevice.address.equals(address, true)) {
                crashlyticsReport.log("Main device is ${device.name}")
                deviceInfoRepository.putDevice(device, isPrimary = false)
            }

            crashlyticsReport.log("Start receiving device versions")
            val (hardware, software) = deviceRepository.getVersions(address)

            crashlyticsReport.log("Start saving device versions to storage")
            val deviceInfo = deviceData?.second?.copy(
                hardwareVersion = hardware,
                softwareVersion = software
            ) ?: GlucometerInfo(id = address, hardwareVersion = hardware, softwareVersion = software)

            deviceInfoRepository.updateGlucometerInfo(deviceInfo, null)

        } finally {
            crashlyticsReport.log("The procedure for disconnecting the connection with the device has begun")
            deviceRepository.disconnect()
        }
    }

    data class Params(val device: Glucometer, val pinCode: String)
}

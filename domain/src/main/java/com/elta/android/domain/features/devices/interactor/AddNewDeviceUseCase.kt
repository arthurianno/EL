package com.elta.android.domain.features.devices.interactor

import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
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
        crashlyticsReport.log("adding device with address: ${params?.device?.address}")
        try {
            bluetoothStateRepository.checkBluetoothAvailabilityAndPermissions(crashlyticsReport)

            val (device, pincode) = requireNotNull(params) {
                val errorString = "params for new device cannot be null"
                crashlyticsReport.writeException(RuntimeException(errorString))
                errorString
            }
            val address = device.address

            deviceRepository.connectWithTimeout(address, pincode, false, crashlyticsReport)

            crashlyticsReport.log("saving pin")
            pinRepository.savePin(address, pincode)

            crashlyticsReport.log("starting getting primary device")
            val primaryDevice = deviceInfoRepository.getPrimaryDeviceWithLastEvent()?.first
            crashlyticsReport.log("primary device obtained")

            if (primaryDevice == null) {
                crashlyticsReport.log("obtained device is null")
                deviceInfoRepository.putDevice(glucometer = device, isPrimary = true)
            } else if (!primaryDevice.address.equals(address, true)) {
                crashlyticsReport.log("obtained device is device ${device.name}")
                deviceInfoRepository.putDevice(device, isPrimary = false)
            }
        } finally {
            crashlyticsReport.log("disconnecting device")
            deviceRepository.disconnect()
        }
    }

    data class Params(val device: Glucometer, val pinCode: String)
}

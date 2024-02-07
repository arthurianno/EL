package com.elta.android.domain.features.devices.interactor

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
    schedulers: SchedulersFacade
) : CompletableUseCase<AddNewDeviceUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Completable {
        return rxCompletable(EmptyCoroutineContext + Dispatchers.Unconfined) {
            addDevice(params)
        }
    }

    private suspend fun addDevice(params: Params?) {
        try {
            //TODO: Добавить логгер который будет логгировать ошибки внутри проверки
            bluetoothStateRepository.checkBluetoothAvailabilityAndPermissions()

            val (device, pincode) = requireNotNull(params) {
                //TODO: лог, но пинкод не логгировать
                "params for new device cannot be null"
            }
            val address = device.address

            deviceRepository.connectWithTimeout(address, pincode)
            pinRepository.savePin(address, pincode)

            val primaryDevice = deviceInfoRepository.getPrimaryDeviceWithLastEvent()?.first

            if (primaryDevice == null) {
                deviceInfoRepository.putDevice(glucometer = device, isPrimary = true)
            } else if (!primaryDevice.address.equals(address, true)) {
                deviceInfoRepository.putDevice(device, isPrimary = false)
            }
        } finally {
            deviceRepository.disconnect()
        }
    }

    data class Params(val device: Glucometer, val pinCode: String)
}

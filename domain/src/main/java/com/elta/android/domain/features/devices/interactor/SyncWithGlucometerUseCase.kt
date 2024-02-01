package com.elta.android.domain.features.devices.interactor

import com.elta.android.common.errors.GlucometerConnectionException
import com.elta.android.common.errors.GlucometerNotConnectedException
import com.elta.android.common.errors.GlucometerPinNotFoundInternaly
import com.elta.android.common.errors.PrimaryGlucometerNotFoundError
import com.elta.android.domain.features.devices.checkBluetoothAvailabilityAndPermissions
import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.repository.BluetoothStateRepository
import com.elta.android.domain.features.devices.repository.DeviceInfoRepository
import com.elta.android.domain.features.devices.repository.DeviceRepository
import com.elta.android.domain.features.devices.repository.PinRepository
import com.elta.android.domain.features.user.repository.ProfileRepository
import com.nullgr.core.interactor.ObservableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx2.rxObservable
import javax.inject.Inject
import kotlin.coroutines.EmptyCoroutineContext

class SyncWithGlucometerUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val deviceInfoRepository: DeviceInfoRepository,
    private val bluetoothStateRepository: BluetoothStateRepository,
    private val profileRepository: ProfileRepository,
    private val pinRepository: PinRepository,
    schedulers: SchedulersFacade
) : ObservableUseCase<Int, SyncWithGlucometerUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Observable<Int> {
        return rxObservable(EmptyCoroutineContext + Dispatchers.Unconfined) {
            val deviceWithLastEvent = deviceInfoRepository.getPrimaryDeviceWithLastEvent()
            if (deviceWithLastEvent == null) {
                //TODO: в логи
                throw PrimaryGlucometerNotFoundError
            }

            //TODO: Добавить логгер который будет логгировать ошибки внутри проверки
            bluetoothStateRepository.checkBluetoothAvailabilityAndPermissions()

            val profile = try {
                profileRepository.getProfile().blockingGet()
            } catch (exception: Exception) {
                //TODO: в логи
                throw exception
            }

            val address = params?.device?.address ?: deviceWithLastEvent.first.address
            val lastSyncEvent = deviceWithLastEvent.second.lastSyncEvent
            val email = profile.email

            if (email.isNullOrBlank()) {
                throw Exception("Email is empty")
            }

            syncWithDevice(address, email, lastSyncEvent)
        }
    }

    private suspend fun syncWithDevice(deviceAddress: String, userEmail: String, lastSyncEvent: String?): Int {
        val pinCode = pinRepository.getPin(deviceAddress)
        if (pinCode == null) {
            //TODO: В логи
            throw GlucometerPinNotFoundInternaly
        }

        try {
            deviceRepository.connectDevice(deviceAddress, pinCode)

            val glucometerInfo = runActionWithReconnection(deviceAddress = deviceAddress, pinCode = pinCode) {
                deviceRepository.getGlucometerInfo(deviceAddress)
            }

            val events = runActionWithReconnection(deviceAddress = deviceAddress, pinCode = pinCode) {
                deviceRepository.syncWithDevice(deviceAddress, userEmail, glucometerInfo.glucometerSerialNumber, lastSyncEvent)
            }

            deviceInfoRepository.updateGlucometerInfo(glucometerInfo, events.firstOrNull())

            //TODO: Сейчас это callable который по сути нифига не делает, исправить
            /*eventsRepository.addEventFromGlucometer(events)*/

            return events.size
        } finally {
            deviceRepository.disconnect()
        }

    }

    private suspend fun <T> runActionWithReconnection(
        deviceAddress: String,
        pinCode: String,
        repeatTimes: Int = 0,
        maxRepeatTimes: Int = 3, action: suspend () -> T
    ): T {
        return try {
            action()
        } catch (e: Exception) {
            if (e is GlucometerNotConnectedException) {
                if (repeatTimes >= maxRepeatTimes) throw GlucometerConnectionException(deviceAddress)
                deviceRepository.connectDevice(deviceAddress, pinCode)
                runActionWithReconnection(
                    deviceAddress,
                    pinCode,
                    repeatTimes + 1,
                    maxRepeatTimes,
                    action
                )
            } else {
                throw e
            }
        }
    }


    data class Params(val device: Glucometer? = null)
}
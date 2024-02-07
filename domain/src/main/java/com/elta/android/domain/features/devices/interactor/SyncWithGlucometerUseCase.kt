package com.elta.android.domain.features.devices.interactor

import com.elta.android.common.errors.GlucometerPinNotFoundInternaly
import com.elta.android.common.errors.PrimaryGlucometerNotFoundError
import com.elta.android.domain.features.devices.checkBluetoothAvailabilityAndPermissions
import com.elta.android.domain.features.devices.connectWithTimeout
import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.repository.BluetoothStateRepository
import com.elta.android.domain.features.devices.repository.DeviceInfoRepository
import com.elta.android.domain.features.devices.repository.DeviceRepository
import com.elta.android.domain.features.devices.repository.PinRepository
import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.elta.android.domain.features.user.repository.ProfileRepository
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.rx2.rxObservable
import javax.inject.Inject
import kotlin.coroutines.EmptyCoroutineContext

class SyncWithGlucometerUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val deviceInfoRepository: DeviceInfoRepository,
    private val bluetoothStateRepository: BluetoothStateRepository,
    private val profileRepository: ProfileRepository,
    private val pinRepository: PinRepository,
    private val eventsRepository: EventsRepository,
    schedulers: SchedulersFacade
) : ObservableWithTimerUseCase<Int, SyncWithGlucometerUseCase.Params>(schedulers) {

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

            syncWithDevice(this, address, email, lastSyncEvent)
        }
    }

    private suspend fun syncWithDevice(
        scope: ProducerScope<Int>,
        deviceAddress: String,
        userEmail: String,
        lastSyncEvent: String?
    ): Int {
        val pinCode = pinRepository.getPin(deviceAddress)
        if (pinCode == null) {
            //TODO: В логи
            throw GlucometerPinNotFoundInternaly
        }

        try {
            deviceRepository.connectWithTimeout(deviceAddress, pinCode)

            resetAndLaunchTimer(scope)
            val glucometerInfo = deviceRepository.getGlucometerInfo(deviceAddress)


            resetAndLaunchTimer(scope)
            val events = deviceRepository.syncWithDevice(
                address = deviceAddress,
                email = userEmail,
                serial = glucometerInfo.glucometerSerialNumber,
                lastSyncEvent = lastSyncEvent
            ) {
                resetAndLaunchTimer(scope)
            }

            deviceInfoRepository.updateGlucometerInfo(glucometerInfo, events.firstOrNull())

            if (events.isNotEmpty()) {
                eventsRepository.addEventFromGlucometer(events)
            }

            return events.size
        } finally {
            deviceRepository.disconnect()
            cancelTimer()
        }

    }

    data class Params(val device: Glucometer? = null)
}
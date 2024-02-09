package com.elta.android.domain.features.devices.interactor

import com.elta.android.common.errors.GlucometerPinNotFoundInternaly
import com.elta.android.common.errors.PrimaryGlucometerNotFoundError
import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
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
    private val crashlyticsReport: CrashlyticsReport,
    schedulers: SchedulersFacade
) : ObservableWithTimerUseCase<Int, SyncWithGlucometerUseCase.Params>(schedulers, crashlyticsReport) {

    override fun buildUseCaseObservable(params: Params?): Observable<Int> {
        return rxObservable(EmptyCoroutineContext + Dispatchers.Unconfined) {
            crashlyticsReport.log("starting sync with glucometer ${params?.device?.address}")
            crashlyticsReport.log("getting primary device")
            val deviceWithLastEvent = deviceInfoRepository.getPrimaryDeviceWithLastEvent()
            if (deviceWithLastEvent == null) {
                crashlyticsReport.writeException(PrimaryGlucometerNotFoundError)
                throw PrimaryGlucometerNotFoundError
            }

            bluetoothStateRepository.checkBluetoothAvailabilityAndPermissions(crashlyticsReport)

            crashlyticsReport.log("getting profile info")
            val profile = try {
                profileRepository.getProfile().blockingGet()
            } catch (exception: Exception) {
                crashlyticsReport.writeException(exception)
                throw exception
            }

            crashlyticsReport.log("preparing data for sync")
            val address = params?.device?.address ?: deviceWithLastEvent.first.address
            val lastSyncEvent = deviceWithLastEvent.second.lastSyncEvent
            val email = profile.email

            if (email.isNullOrBlank()) {
                val exception = Exception("Email is empty")
                crashlyticsReport.writeException(exception)
                throw exception
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
        crashlyticsReport.log("getting pin")
        val pinCode = pinRepository.getPin(deviceAddress)
        if (pinCode == null) {
            crashlyticsReport.writeException(GlucometerPinNotFoundInternaly)
            throw GlucometerPinNotFoundInternaly
        }

        try {
            deviceRepository.connectWithTimeout(deviceAddress, pinCode, false, crashlyticsReport)

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

            crashlyticsReport.log("start updating device info to storage")
            deviceInfoRepository.updateGlucometerInfo(glucometerInfo, events.firstOrNull())

            if (events.isNotEmpty()) {
                crashlyticsReport.log("start sending events to backend and db")
                eventsRepository.addEventFromGlucometer(events)
            }

            return events.size
        } finally {
            crashlyticsReport.log("disconnection and timer cancelation")
            deviceRepository.disconnect()
            cancelTimer()
        }

    }

    data class Params(val device: Glucometer? = null)
}
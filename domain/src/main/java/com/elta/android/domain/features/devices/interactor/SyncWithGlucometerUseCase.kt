package com.elta.android.domain.features.devices.interactor

import com.elta.android.common.errors.GlucometerPinNotFoundInternaly
import com.elta.android.common.errors.PrimaryGlucometerNotFoundError
import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
import com.elta.android.common.utils.hideMac
import com.elta.android.domain.features.appsettings.AppSettingsRepository
import com.elta.android.domain.features.devices.SEND_DATA_TIMEOUT
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
import timber.log.Timber
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject
import kotlin.coroutines.EmptyCoroutineContext

import com.elta.android.domain.features.devices.model.GlucometerSyncResult

class SyncWithGlucometerUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val deviceInfoRepository: DeviceInfoRepository,
    private val bluetoothStateRepository: BluetoothStateRepository,
    private val appSettingsRepository: AppSettingsRepository,
    private val profileRepository: ProfileRepository,
    private val pinRepository: PinRepository,
    private val eventsRepository: EventsRepository,
    private val crashlyticsReport: CrashlyticsReport,
    schedulers: SchedulersFacade
) : ObservableWithTimerUseCase<GlucometerSyncResult, SyncWithGlucometerUseCase.Params>(schedulers, crashlyticsReport) {

    override fun buildUseCaseObservable(params: Params?): Observable<GlucometerSyncResult> {
        return rxObservable(EmptyCoroutineContext + Dispatchers.Unconfined) {
            crashlyticsReport.log("The synchronization procedure has begun with the device ${params?.device?.address?.hideMac()}")
            crashlyticsReport.log("Receiving data for the main device")
            val deviceWithLastEvent = deviceInfoRepository.getPrimaryDeviceWithLastEvent()
            if (deviceWithLastEvent == null) {
                crashlyticsReport.writeException(PrimaryGlucometerNotFoundError)
                throw PrimaryGlucometerNotFoundError
            }

            bluetoothStateRepository.checkBluetoothAvailabilityAndPermissions(
                crashlyticsReport = crashlyticsReport,
                isLocationNeeded = true
            )
            crashlyticsReport.log("Getting a user profile")
            val profile = try {
                profileRepository.getProfile().blockingGet()
            } catch (exception: Exception) {
                crashlyticsReport.writeException(exception)
                throw exception
            }

            crashlyticsReport.log("Preparing data for synchronization")
            val address = params?.device?.address ?: deviceWithLastEvent.first.address
            val glucometerName = params?.device?.name ?: deviceWithLastEvent.first.name
            val lastSyncEvent = deviceWithLastEvent.second.lastSyncEvent
            val email = profile.email

            if (email.isNullOrBlank()) {
                val exception = Exception("Email is empty")
                crashlyticsReport.writeException(exception)
                throw exception
            }

            syncWithDevice(this, address, email, lastSyncEvent, glucometerName)
        }
    }

    private suspend fun syncWithDevice(
        scope: ProducerScope<GlucometerSyncResult>,
        deviceAddress: String,
        userEmail: String,
        lastSyncEvent: String?,
        glucometerName: String?
    ) {
        crashlyticsReport.log("Getting pin")
        val pinCode = pinRepository.getPin(deviceAddress)
        if (pinCode == null) {
            crashlyticsReport.writeException(GlucometerPinNotFoundInternaly)
            throw GlucometerPinNotFoundInternaly
        }

        try {
            deviceRepository.connectWithTimeout(deviceAddress, pinCode, crashlyticsReport)

            resetAndLaunchTimer(scope)
            val glucometerInfo = deviceRepository.getGlucometerInfo(deviceAddress)

            resetAndLaunchTimer(scope)
            val measurements = deviceRepository.syncWithDevice(
                address = deviceAddress,
                lastSyncEvent = lastSyncEvent
            ) {
                resetAndLaunchTimer(scope)
            }

            resetAndLaunchTimer(scope, SEND_DATA_TIMEOUT)
            crashlyticsReport.log("Started saving device data to local storage")
            val rawEvents = deviceRepository.buildEvents(
                deviceAddress,
                userEmail,
                glucometerInfo.glucometerSerialNumber,
                measurements,
                glucometerName
            )
            Timber.d("⏰ SyncWithGlucometer: glucometerInfo.isTimeOutOfSync=${glucometerInfo.isTimeOutOfSync}, rawEvents.size=${rawEvents.size}")
            var invalidIndex = 0L
            val syncTime = ZonedDateTime.now(ZoneOffset.UTC)
            val events = rawEvents.map { event ->
                val isInvalid = event.isTimeInvalid || glucometerInfo.isTimeOutOfSync
                if (isInvalid) {
                    val adjustedDate = syncTime.minusMinutes(invalidIndex++)
                    event.copy(
                        date = adjustedDate,
                        isTimeInvalid = true
                    )
                } else {
                    event
                }
            }
            events.forEach { event ->
                Timber.d("⏰ SyncWithGlucometer event: id=${event.id}, date=${event.date}, isTimeInvalid=${event.isTimeInvalid}")
            }
            deviceInfoRepository.updateGlucometerInfo(glucometerInfo, events.firstOrNull())

            resetAndLaunchTimer(scope, SEND_DATA_TIMEOUT)
            if (measurements.isNotEmpty()) {
                crashlyticsReport.log("Started sending measurements to the backend and saving to local storage")
                eventsRepository.addEventFromGlucometer(events)

                resetAndLaunchTimer(scope, SEND_DATA_TIMEOUT)
            }

            val hasInvalidTime = events.any { it.isTimeInvalid } || (measurements.isNotEmpty() && glucometerInfo.isTimeOutOfSync)
            scope.channel.send(GlucometerSyncResult(count = measurements.size, hasInvalidTime = hasInvalidTime))
        } finally {
            crashlyticsReport.log("The procedure for disconnecting the connection and stopping the timers has begun")
            deviceRepository.disconnect()
            cancelTimer()
        }

    }

    data class Params(val device: Glucometer? = null)
}
package com.elta.android.domain.features.rostech.interactor

import com.elta.android.common.errors.GlucometerPinNotFoundInternaly
import com.elta.android.common.errors.NotFoundError
import com.elta.android.common.errors.PrimaryGlucometerNotFoundError
import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
import com.elta.android.domain.features.devices.checkBluetoothAvailabilityAndPermissions
import com.elta.android.domain.features.devices.repository.BluetoothStateRepository
import com.elta.android.domain.features.devices.repository.DeviceInfoRepository
import com.elta.android.domain.features.devices.repository.PinRepository
import com.elta.android.domain.features.rostech.repository.IomtRepository
import com.elta.android.domain.features.user.repository.ProfileRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import io.reactivex.CompletableEmitter
import javax.inject.Inject

class ConnectIomtUseCase @Inject constructor(
    private val rosTech: IomtRepository,
    private val deviceInfoRepository: DeviceInfoRepository,
    private val profileRepository: ProfileRepository,
    private val pinRepository: PinRepository,
    private val crashlyticsReport: CrashlyticsReport,
    private val bluetoothStateRepository: BluetoothStateRepository,
    schedulers: SchedulersFacade
) : CompletableUseCase<Unit>(schedulers) {
    override fun buildUseCaseObservable(params: Unit?): Completable {
        return Completable.create { emitter ->
            try {
                bluetoothStateRepository.checkBluetoothAvailabilityAndPermissions(crashlyticsReport)

                rosTech.setListeners(
                    onDisconnect = { emitter.onComplete() },
                    onException = { ex -> writeException(ex, emitter) }
                )

                val email = profileRepository.getProfile().blockingGet().email
                    ?: throw NotFoundError("Email not found")
                val primaryDevice = deviceInfoRepository.getPrimaryDeviceWithLastEvent()
                val address = primaryDevice?.first?.address ?: throw PrimaryGlucometerNotFoundError
                val pin = pinRepository.getPin(address) ?: throw GlucometerPinNotFoundInternaly

                rosTech.connect(pin, address, email)

            } catch (ex: Exception) {
                writeException(ex, emitter)
            }
        }
    }

    private fun writeException(ex: Exception, emitter: CompletableEmitter) {
        crashlyticsReport.writeException(ex)
        emitter.onComplete()
    }
}

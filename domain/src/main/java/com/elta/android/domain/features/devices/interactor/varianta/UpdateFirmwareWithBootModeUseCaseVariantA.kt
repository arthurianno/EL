package com.elta.android.domain.features.devices.interactor

import com.elta.android.common.errors.GlucometerPinNotFoundInternaly
import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
import com.elta.android.domain.features.devices.checkBluetoothAvailabilityAndPermissions
import com.elta.android.domain.features.devices.repository.BluetoothStateRepositoryVariantA
import com.elta.android.domain.features.devices.repository.PinRepository
import com.elta.android.domain.features.devices.repository.UpdateRepository
import com.elta.android.domain.features.firmware.model.FirmwareFile
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx2.rxObservable
import javax.inject.Inject
import kotlin.coroutines.EmptyCoroutineContext

// fixme Variant A : improved_enabling_location
class UpdateFirmwareWithBootModeUseCaseVariantA @Inject constructor(
    private val updateRepository: UpdateRepository,
    private val pinRepository: PinRepository,
    private val bluetoothStateRepository: BluetoothStateRepositoryVariantA,
    private val crashlyticsReport: CrashlyticsReport,
    schedulers: SchedulersFacade
) : ObservableWithTimerUseCase<String, UpdateFirmwareWithBootModeUseCaseVariantA.Params>(
    schedulers,
    crashlyticsReport
) {

    override fun buildUseCaseObservable(params: Params?): Observable<String> {
        return rxObservable(EmptyCoroutineContext + Dispatchers.Unconfined) {
            bluetoothStateRepository.checkBluetoothAvailabilityAndPermissions(crashlyticsReport)

            crashlyticsReport.log("Checking parameters for nullability")
            val data = checkNotNull(params)

            crashlyticsReport.log("Checking pin")
            val pinCode = pinRepository.getPin(data.address)
            if (pinCode.isNullOrEmpty()) {
                val error = GlucometerPinNotFoundInternaly
                crashlyticsReport.writeException(error)
                throw error
            }

            updateRepository.updateFirmwareWithBootMode(
                address = data.address,
                pin = pinCode,
                firmwareFile = data.file
            )
        }
    }

    data class Params(
        val address: String,
        val file: FirmwareFile
    )
}

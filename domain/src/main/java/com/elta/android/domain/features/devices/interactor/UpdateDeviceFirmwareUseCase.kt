package com.elta.android.domain.features.devices.interactor

import com.elta.android.domain.features.devices.checkBluetoothAvailabilityAndPermissions
import com.elta.android.domain.features.devices.repository.BluetoothStateRepository
import com.elta.android.domain.features.devices.repository.UpdateRepository
import com.elta.android.domain.features.firmware.model.FirmwareFile
import com.nullgr.core.interactor.ObservableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx2.rxObservable
import javax.inject.Inject
import kotlin.coroutines.EmptyCoroutineContext

class UpdateDeviceFirmwareUseCase @Inject constructor(
    private val repo: UpdateRepository,
    private val bluetoothStateRepository: BluetoothStateRepository,
    schedulers: SchedulersFacade
) : ObservableUseCase<String, UpdateDeviceFirmwareUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Observable<String> {
        bluetoothStateRepository.checkBluetoothAvailabilityAndPermissions()

        return rxObservable(EmptyCoroutineContext + Dispatchers.Unconfined) {
            val p = checkNotNull(params)
            repo.updateFirmware(p.address, p.file)
        }
    }

    data class Params(
        val address: String,
        val file: FirmwareFile
    )
}

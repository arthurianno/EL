package com.elta.android.domain.features.devices.interactor

import com.elta.android.domain.features.devices.repository.DeviceRepository
import com.elta.android.domain.features.firmware.model.FirmwareFile
import com.nullgr.core.interactor.ObservableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Observable
import javax.inject.Inject

class UpdateDeviceFirmwareUseCase @Inject constructor(
    private val repo: DeviceRepository,
    schedulers: SchedulersFacade
) : ObservableUseCase<String, UpdateDeviceFirmwareUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Observable<String> {
        val p = checkNotNull(params)
        return repo.updateFirmware(p.address, p.file)
    }

    data class Params(
        val address: String,
        val file: FirmwareFile
    )
}

package com.elta.android.domain.features.devices.interactor

import com.elta.android.domain.features.devices.repository.DeviceRepository
import com.elta.android.domain.features.firmware.model.FirmwareFile
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import javax.inject.Inject

class UpdateDeviceFirmwareUseCase @Inject constructor(
    private val repo: DeviceRepository,
    schedulers: SchedulersFacade
) : CompletableUseCase<UpdateDeviceFirmwareUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Completable {
        val p = checkNotNull(params)
        return repo.updateFirmware(p.address, p.file)
    }

    data class Params(
        val address: String,
        val file: FirmwareFile
    )
}
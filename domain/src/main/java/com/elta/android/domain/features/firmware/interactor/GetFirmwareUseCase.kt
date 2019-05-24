package com.elta.android.domain.features.firmware.interactor

import com.elta.android.domain.features.firmware.model.Firmware
import com.elta.android.domain.features.firmware.model.FirmwareFile
import com.elta.android.domain.features.firmware.repository.FirmwareRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class GetFirmwareUseCase @Inject constructor(
    private val repo: FirmwareRepository,
    schedulers: SchedulersFacade
) : SingleUseCase<FirmwareFile, GetFirmwareUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Single<FirmwareFile> =
        repo.getFirmware(checkNotNull(params).firmware)

    data class Params(val firmware: Firmware)
}
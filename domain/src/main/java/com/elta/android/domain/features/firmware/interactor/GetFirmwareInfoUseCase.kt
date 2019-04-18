package com.elta.android.domain.features.firmware.interactor

import com.elta.android.domain.features.firmware.model.Firmware
import com.elta.android.domain.features.firmware.repository.FirmwareRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class GetFirmwareInfoUseCase @Inject constructor(
    private val repo: FirmwareRepository,
    schedulers: SchedulersFacade
) : SingleUseCase<Firmware, Unit>(schedulers) {

    override fun buildUseCaseObservable(params: Unit?): Single<Firmware> =
        repo.getFirmwareInfo()
}
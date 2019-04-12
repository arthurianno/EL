package com.elta.android.domain.features.firmware.interactor

import com.elta.android.domain.features.firmware.repository.FirmwareRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import java.io.File
import javax.inject.Inject

class GetFirmwareUseCase @Inject constructor(
    private val repo: FirmwareRepository,
    schedulers: SchedulersFacade
) : SingleUseCase<File, GetFirmwareUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Single<File> =
        repo.getFirmware(checkNotNull(params).version)

    data class Params(val version: String)
}
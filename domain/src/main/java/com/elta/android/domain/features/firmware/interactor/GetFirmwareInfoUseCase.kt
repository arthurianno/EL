package com.elta.android.domain.features.firmware.interactor

import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.firmware.model.FirmwareInfo
import com.elta.android.domain.features.firmware.repository.FirmwareRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class GetFirmwareInfoUseCase @Inject constructor(
    private val repo: FirmwareRepository,
    schedulers: SchedulersFacade
) : SingleUseCase<FirmwareInfo, GetFirmwareInfoUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Single<FirmwareInfo> {
        val p = checkNotNull(params)
        return repo.getFirmwareInfo(
            glucometerInfo = p.glucometerInfo,
        )
    }

    data class Params(
        val modelId: String,
        val currentVersion: String,
        val glucometerInfo: GlucometerInfo,
    )
}

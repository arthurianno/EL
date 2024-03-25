package com.elta.android.domain.features.devices.interactor

import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.devices.repository.DeviceInfoRepository
import com.elta.android.domain.features.devices.repository.DeviceRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class GetLastGlucometerInfoUseCase @Inject constructor(
    private val repo: DeviceInfoRepository,
    schedulers: SchedulersFacade
) : SingleUseCase<GlucometerInfo, GetLastGlucometerInfoUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Single<GlucometerInfo> = Single.fromCallable {
        repo.getLastDeviceInfo(checkNotNull(params).address)
    }

    data class Params(val address: String)
}

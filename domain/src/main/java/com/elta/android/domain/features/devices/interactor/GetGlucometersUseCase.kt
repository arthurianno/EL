package com.elta.android.domain.features.devices.interactor

import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.devices.repository.DeviceRepository
import com.nullgr.core.interactor.SingleListUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class GetGlucometersUseCase @Inject constructor(
    private val repo: DeviceRepository,
    schedulers: SchedulersFacade
) : SingleListUseCase<Pair<Glucometer, GlucometerInfo>, Unit>(schedulers) {

    override fun buildUseCaseObservable(params: Unit?): Single<List<Pair<Glucometer, GlucometerInfo>>> =
        repo.getDevices()
}

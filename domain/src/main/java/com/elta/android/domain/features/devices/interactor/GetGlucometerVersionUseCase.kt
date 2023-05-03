package com.elta.android.domain.features.devices.interactor

import com.elta.android.domain.features.devices.repository.DeviceRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class GetGlucometerVersionUseCase @Inject constructor(
    private val repo: DeviceRepository,
    schedulers: SchedulersFacade
) : SingleUseCase<String, Unit>(schedulers) {

    override fun buildUseCaseObservable(params: Unit?): Single<String> =
        repo.getDevices()
            .map { it.find { glucometer -> glucometer.first.isPrimary } }
            .map { it.second.softwareVersion.orEmpty() }
}

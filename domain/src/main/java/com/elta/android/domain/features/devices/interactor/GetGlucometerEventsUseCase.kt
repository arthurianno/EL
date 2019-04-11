package com.elta.android.domain.features.devices.interactor

import com.elta.android.domain.features.devices.repository.DeviceRepository
import com.nullgr.core.interactor.SingleListUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class GetGlucometerEventsUseCase @Inject constructor(
    private val repo: DeviceRepository,
    schedulers: SchedulersFacade
) : SingleListUseCase<String, GetGlucometerEventsUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Single<List<String>> =
        repo.getDeviceEvents(checkNotNull(params).address)

    data class Params(val address: String)
}
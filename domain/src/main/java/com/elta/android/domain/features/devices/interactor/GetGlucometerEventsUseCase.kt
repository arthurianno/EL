package com.elta.android.domain.features.devices.interactor

import com.elta.android.domain.features.devices.repository.DeviceRepository
import com.elta.android.domain.features.diary.events.model.Event
import com.nullgr.core.interactor.SingleListUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class GetGlucometerEventsUseCase @Inject constructor(
    private val repo: DeviceRepository,
    schedulers: SchedulersFacade
) : SingleListUseCase<Event, Unit>(schedulers) {

    override fun buildUseCaseObservable(params: Unit?): Single<List<Event>> =
        repo.getDeviceEvents()
}
package com.elta.android.domain.features.devices.interactor

import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.repository.DeviceRepository
import com.nullgr.core.interactor.ObservableListUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Observable
import javax.inject.Inject

class FindGlucometersUseCase @Inject constructor(
    private val repo: DeviceRepository,
    schedulers: SchedulersFacade
) : ObservableListUseCase<Glucometer, Unit>(schedulers) {

    override fun buildUseCaseObservable(params: Unit?): Observable<List<Glucometer>> =
        repo.findDevices()
}

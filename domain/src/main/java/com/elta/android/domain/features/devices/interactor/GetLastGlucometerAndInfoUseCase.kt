package com.elta.android.domain.features.devices.interactor

import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.devices.repository.DeviceRepository
import com.nullgr.core.interactor.ObservableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import javax.inject.Inject

class GetLastGlucometerAndInfoUseCase @Inject constructor(
    private val repo: DeviceRepository,
    schedulers: SchedulersFacade
) : ObservableUseCase<Pair<Glucometer, GlucometerInfo>, GetLastGlucometerAndInfoUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Observable<Pair<Glucometer, GlucometerInfo>> {
        val p = checkNotNull(params)
        return Observables.zip(
            repo.getDevice(p.address).toObservable(),
            repo.getLastDeviceInfo(p.address).toObservable()
        )
    }

    data class Params(val address: String)
}

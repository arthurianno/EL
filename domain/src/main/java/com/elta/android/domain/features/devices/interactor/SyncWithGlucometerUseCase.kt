package com.elta.android.domain.features.devices.interactor

import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.repository.DeviceRepository
import com.nullgr.core.interactor.ObservableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Observable
import javax.inject.Inject

class SyncWithGlucometerUseCase @Inject constructor(
    private val repo: DeviceRepository,
    schedulers: SchedulersFacade
) : ObservableUseCase<Int, SyncWithGlucometerUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Observable<Int> {
        val p = checkNotNull(params)
        return repo.syncWithDevice(p.device)
    }

    data class Params(val device: Glucometer? = null)
}

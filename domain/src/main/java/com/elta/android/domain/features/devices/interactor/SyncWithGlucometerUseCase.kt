package com.elta.android.domain.features.devices.interactor

import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.repository.DeviceRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import javax.inject.Inject

class SyncWithGlucometerUseCase @Inject constructor(
    private val repo: DeviceRepository,
    schedulers: SchedulersFacade
) : CompletableUseCase<SyncWithGlucometerUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Completable {
        val p = checkNotNull(params)
        return repo.syncWithDevice(p.device)
    }

    data class Params(val device: Glucometer?)
}
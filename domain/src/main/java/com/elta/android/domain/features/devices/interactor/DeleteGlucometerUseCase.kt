package com.elta.android.domain.features.devices.interactor

import com.elta.android.domain.features.devices.repository.DeviceRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import javax.inject.Inject

class DeleteGlucometerUseCase @Inject constructor(
    private val repo: DeviceRepository,
    schedulers: SchedulersFacade
) : CompletableUseCase<DeleteGlucometerUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Completable =
        repo.deleteDevice(checkNotNull(params).address)

    data class Params(val address: String)
}

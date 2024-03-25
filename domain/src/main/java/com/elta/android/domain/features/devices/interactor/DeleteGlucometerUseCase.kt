package com.elta.android.domain.features.devices.interactor

import com.elta.android.domain.features.devices.repository.DeviceInfoRepository
import com.elta.android.domain.features.devices.repository.DeviceRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import javax.inject.Inject

class DeleteGlucometerUseCase @Inject constructor(
    private val repo: DeviceInfoRepository,
    schedulers: SchedulersFacade
) : CompletableUseCase<DeleteGlucometerUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Completable = Completable.fromCallable {
        repo.deleteDevice(checkNotNull(params).address)
    }

    data class Params(val address: String)
}

package com.elta.android.domain.features.devices.interactor

import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.repository.DeviceRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import javax.inject.Inject

class ConnectDeviceUseCase @Inject constructor(
    private val repo: DeviceRepository,
    schedulers: SchedulersFacade
) : CompletableUseCase<ConnectDeviceUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Completable {
        val p = checkNotNull(params)
        return repo.connectDevice(p.device, p.pinCode)
    }

    data class Params(val device: Glucometer, val pinCode: String)
}
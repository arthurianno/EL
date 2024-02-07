package com.elta.android.domain.features.devices.interactor

import com.elta.android.domain.features.devices.repository.DeviceInfoRepository
import com.nullgr.core.interactor.ObservableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Observable
import javax.inject.Inject

class CheckConnectedDevicesUseCase @Inject constructor(
    private val deviceInfoRepository: DeviceInfoRepository,
    schedulers: SchedulersFacade
) : ObservableUseCase<Boolean, CheckConnectedDevicesUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Observable<Boolean> {
        val address = checkNotNull(params).address
        val devices = deviceInfoRepository.getDevices()

        val containsSelected = devices.any { it.first.address == address }
        return Observable.just(containsSelected)
    }

    data class Params(val address: String?)
}

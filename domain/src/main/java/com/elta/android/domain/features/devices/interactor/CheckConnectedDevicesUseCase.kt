package com.elta.android.domain.features.devices.interactor

import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
import com.elta.android.domain.features.devices.repository.DeviceInfoRepository
import com.nullgr.core.interactor.ObservableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Observable
import javax.inject.Inject

class CheckConnectedDevicesUseCase @Inject constructor(
    private val deviceInfoRepository: DeviceInfoRepository,
    private val crashlyticsReport: CrashlyticsReport,
    schedulers: SchedulersFacade
) : ObservableUseCase<Boolean, CheckConnectedDevicesUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Observable<Boolean> {
        crashlyticsReport.log("start checking device in connected")
        val address = checkNotNull(params).address
        crashlyticsReport.log("getting devices")
        val devices = deviceInfoRepository.getDevices()

        crashlyticsReport.log("searching device in connected devices")
        val containsSelected = devices.any { it.first.address == address }
        crashlyticsReport.log("is device already connected: $containsSelected")
        return Observable.just(containsSelected)
    }

    data class Params(val address: String?)
}

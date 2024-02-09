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
        crashlyticsReport.log("Started checking the device for its presence in the list of connected devices")
        val address = checkNotNull(params).address
        crashlyticsReport.log("Getting a list of connected devices")
        val devices = deviceInfoRepository.getDevices()

        crashlyticsReport.log("Searching for a new device in the list of already connected ones")
        val containsSelected = devices.any { it.first.address == address }
        crashlyticsReport.log("New device status: already connected = $containsSelected")
        return Observable.just(containsSelected)
    }

    data class Params(val address: String?)
}

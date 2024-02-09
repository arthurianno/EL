package com.elta.android.domain.features.devices.interactor

import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.devices.repository.DeviceInfoRepository
import com.nullgr.core.interactor.ObservableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx2.rxObservable
import javax.inject.Inject
import kotlin.coroutines.EmptyCoroutineContext

class GetLastGlucometerAndInfoUseCase @Inject constructor(
    private val repo: DeviceInfoRepository, schedulers: SchedulersFacade,
    private val crashlyticsReport: CrashlyticsReport
) : ObservableUseCase<Pair<Glucometer, GlucometerInfo>, GetLastGlucometerAndInfoUseCase.Params>(
    schedulers
) {

    override fun buildUseCaseObservable(params: Params?): Observable<Pair<Glucometer, GlucometerInfo>> {
        val p = checkNotNull(params)

        return rxObservable(EmptyCoroutineContext + Dispatchers.Unconfined)  {
            crashlyticsReport.log("start getting last glucometer and info")
            val glucometer = repo.getDevice(p.address)
            if (glucometer == null) {
                crashlyticsReport.log("last glucometer not found")
                throw Exception("Glucometer is Empty")
            }
            val glucometerInfo = repo.getLastDeviceInfo(p.address)
            if (glucometerInfo == null) {
                crashlyticsReport.log("last glucometer info not found")
                throw Exception("Glucometer Info is Empty")
            }
            crashlyticsReport.log("last glucometer with info obtained successfully")
            glucometer to glucometerInfo
        }
    }

    data class Params(val address: String)
}

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
        return Observable.fromCallable {
            val p = checkNotNull(params)

            crashlyticsReport.log("Started acquiring data about the latest device")
            val glucometer = repo.getDevice(p.address)
            if (glucometer == null) {
                crashlyticsReport.log("Last device not found")
                throw Exception("Glucometer is Empty")
            }
            val glucometerInfo = repo.getLastDeviceInfo(p.address)
            if (glucometerInfo == null) {
                crashlyticsReport.log("Last device information not found")
                throw Exception("Glucometer Info is Empty")
            }
            crashlyticsReport.log("The last device with information on it was successfully received")
            glucometer to glucometerInfo
        }
    }

    data class Params(val address: String)
}

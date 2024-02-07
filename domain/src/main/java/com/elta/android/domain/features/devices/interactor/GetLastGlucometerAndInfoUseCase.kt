package com.elta.android.domain.features.devices.interactor

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
    private val repo: DeviceInfoRepository, schedulers: SchedulersFacade
) : ObservableUseCase<Pair<Glucometer, GlucometerInfo>, GetLastGlucometerAndInfoUseCase.Params>(
    schedulers
) {

    override fun buildUseCaseObservable(params: Params?): Observable<Pair<Glucometer, GlucometerInfo>> {
        val p = checkNotNull(params)

        return rxObservable(EmptyCoroutineContext + Dispatchers.Unconfined)  {
            val glucometer = repo.getDevice(p.address)
            if (glucometer == null) {
                throw Exception("Glucometer is Empty")
            }
            val glucometerInfo = repo.getLastDeviceInfo(p.address)
            if (glucometerInfo == null) {
                throw Exception("Glucometer Info is Empty")
            }
            glucometer to glucometerInfo
        }
    }

    data class Params(val address: String)
}

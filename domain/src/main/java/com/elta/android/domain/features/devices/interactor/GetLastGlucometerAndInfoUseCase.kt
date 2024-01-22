package com.elta.android.domain.features.devices.interactor

import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.devices.repository.DeviceInfoRepository
import com.nullgr.core.interactor.ObservableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Observable
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.rx2.asObservable
import javax.inject.Inject

class GetLastGlucometerAndInfoUseCase @Inject constructor(
    private val repo: DeviceInfoRepository, schedulers: SchedulersFacade
) : ObservableUseCase<Pair<Glucometer, GlucometerInfo>, GetLastGlucometerAndInfoUseCase.Params>(
    schedulers
) {

    override fun buildUseCaseObservable(params: Params?): Observable<Pair<Glucometer, GlucometerInfo>> {
        //TODO: сделать через корутины или флоу. Сделал временное работоспособное решение.
        val p = checkNotNull(params)

        return flow { emit(repo.getDevice(p.address)) }.zip(flow { emit(repo.getLastDeviceInfo(p.address)) }) { glucometer: Glucometer?, glucometerInfo: GlucometerInfo? ->
            glucometer ?: throw Exception("Glucometer is Empty")
            glucometerInfo ?: throw Exception("Glucometer Info is Empty")
            glucometer to glucometerInfo
        }.asObservable()
    }

    data class Params(val address: String)
}

package com.elta.android.domain.features.devices.interactor

import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
import com.elta.android.domain.features.devices.CONNECT_TIMEOUT
import com.elta.android.domain.features.devices.checkBluetoothAvailabilityAndPermissions
import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.repository.BluetoothStateRepository
import com.elta.android.domain.features.devices.repository.DeviceRepository
import com.nullgr.core.interactor.ObservableListUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Observable
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.rx2.asObservable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FindGlucometersUseCase @Inject constructor(
    private val repo: DeviceRepository,
    private val bluetoothStateRepository: BluetoothStateRepository,
    private val crashlyticsReport: CrashlyticsReport,
    schedulers: SchedulersFacade
) : ObservableListUseCase<Glucometer, Unit>(schedulers) {

    override fun buildUseCaseObservable(params: Unit?): Observable<List<Glucometer>> {
        return repo.findDevices().onStart {
            crashlyticsReport.log("start searching for glucometers")
            bluetoothStateRepository.checkBluetoothAvailabilityAndPermissions(crashlyticsReport)
        }
            .asObservable()
            .timeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
            .doOnError {
                crashlyticsReport.writeException(it)
            }
    }
}

package com.elta.android.domain.features.devices.interactor

import com.elta.android.domain.features.devices.checkBluetoothAvailabilityAndPermissions
import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.repository.BluetoothStateRepository
import com.elta.android.domain.features.devices.repository.DeviceRepository
import com.nullgr.core.interactor.ObservableListUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Observable
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.rx2.asObservable
import javax.inject.Inject

class FindGlucometersUseCase @Inject constructor(
    private val repo: DeviceRepository,
    private val bluetoothStateRepository: BluetoothStateRepository,
    schedulers: SchedulersFacade
) : ObservableListUseCase<Glucometer, Unit>(schedulers) {

    override fun buildUseCaseObservable(params: Unit?): Observable<List<Glucometer>> {
        return repo.findDevices().onStart {
                //TODO: Добавить логгер который будет логгировать ошибки внутри проверки
                bluetoothStateRepository.checkBluetoothAvailabilityAndPermissions()
            }
            .asObservable()
    }
}

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
        // FIXME написать по адекватному. При данной реализации появляются диалоги и запросы на включение блютуза
        // странно то, что он находит устрйоство без включенного блютуза
        // и без разрешения на устройства по близости

        return repo.findDevices().onStart {
                //TODO: Добавить логгер который будет логгировать ошибки внутри проверки
                bluetoothStateRepository.checkBluetoothAvailabilityAndPermissions()
            }
            .asObservable()
        //TODO: тут должна быть фильтрация уже подключенных, но по ТЗ ее не должно быть! Уточнить у Афонина Александра
    }
}
//FIXME: СЕЙЧАС есть задача, что НУЖНО выводить глюкометры, которые уже подсоеденены, а при попытке подключения выдавать ошибку, что уже привязан
// раньше это требование не соблюдалось. Сейчас лучше сразу сделать ПРАВИЛЬНО

// TODO: код для понимания старой фильтрации
//   val connectedDevices = glucometersCache.getAll(CommonConditions.All)
//    private fun filterConnectedDevices(
//        connected: List<GlucometerCachedDto>,
//        results: List<ScanResult>
//    ): List<ScanResult> {
//        val filtered = mutableListOf<ScanResult>()
//        results.forEach { result ->
//            if (connected.firstOrNull { it.address.equals(result.device.address, true) } == null) {
//                filtered.add(result)
//            }
//        }
//        return filtered
//    }

package com.elta.android.domain.features.devices.interactor

import com.elta.android.common.errors.GlucometerSyncError
import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
import com.elta.android.domain.features.appsettings.AppSettingsRepository
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
import java.util.concurrent.TimeoutException
import javax.inject.Inject

class FindGlucometersUseCase @Inject constructor(
    private val repo: DeviceRepository,
    private val bluetoothStateRepository: BluetoothStateRepository,
    private val appSettingsRepository: AppSettingsRepository,
    private val crashlyticsReport: CrashlyticsReport,
    schedulers: SchedulersFacade
) : ObservableListUseCase<Glucometer, FindGlucometersUseCase.Params>(schedulers) {

    private var anyDeviceFound: Boolean = false

    override fun buildUseCaseObservable(params: Params?): Observable<List<Glucometer>> {
        val p = requireNotNull(params)

        return repo.findDevices().onStart {
            crashlyticsReport.log("Started searching for devices in the environment")
            bluetoothStateRepository.checkBluetoothAvailabilityAndPermissions(
                crashlyticsReport = crashlyticsReport,
                // Инициируем проверку разрешения местоположения и включения геолокации в случае,
                // когда этого параметр usecase равен true или когда в локальных настройках этот
                // параметр сохранён как true.
                isLocationNeeded = p.isLocationNeeded || appSettingsRepository.isLocationNeeded
            )
        }
            .asObservable()
            .doOnNext {
                anyDeviceFound = if (p.targetGlucometerName.isNullOrEmpty()){
                    it.isNotEmpty() || anyDeviceFound
                } else {
                    it.find { glucometer -> glucometer.name == p.targetGlucometerName } != null
                }
            }
            .doOnNext {
                if (p.isLocationNeeded) {
                    appSettingsRepository.isLocationNeeded = anyDeviceFound
                }
            }
            .takeUntil(Observable.timer(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .doOnNext {
                    if (!anyDeviceFound) {
                        val exception =
                            GlucometerSyncError(TimeoutException("The search for a device in the environment was stopped due to a timeout, no devices were found"))
                        crashlyticsReport.writeException(exception)
                        throw exception
                    }
                })
    }


    data class Params(
        /** Оповещает о необходимости проверять разрешение на местоположение и включение геолокации для поиска устройства. */
        val isLocationNeeded: Boolean = false,
        /** Имя устройства которое мы ищем. Если null, то находим все устройства из окружения.
         * Если есть имя, то ищем пока не найдём запрашиваемый глюкометр. */
        val targetGlucometerName: String? = null
    )
}

package com.elta.android.presentation.features.home.pm

import com.elta.android.common.errors.BluetoothNotEnabledError
import com.elta.android.common.errors.BluetoothPermissionNotGrantedError
import com.elta.android.common.errors.GlucometerSyncError
import com.elta.android.common.errors.LocationNotEnabledError
import com.elta.android.common.errors.LocationPermissionNotGrantedError
import com.elta.android.domain.features.devices.interactor.SyncWithGlucometerUseCase
import com.elta.android.domain.features.devices.model.GlucometerSyncResult
import com.elta.android.domain.features.emias.interactor.SyncGlucometersUseCase
import com.elta.android.presentation.Events
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.features.sync.control.PermissionsControl2
import com.nullgr.core.rx.RxBus
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableSource

/** Runs device sync and permission recovery; UI decisions stay in HomeFlowPm. */
internal class HomeGlucometerSyncRunner(
    private val syncWithGlucometer: SyncWithGlucometerUseCase,
    private val syncGlucometers: SyncGlucometersUseCase,
    private val permissions: PermissionsControl2,
    private val bus: RxBus,
    private val appMetric: AppMetricTracker,
    private val onInvalidTime: () -> Unit,
    private val onServerSync: () -> Unit,
    private val onManualError: (Throwable) -> Unit,
    private val onAutoError: (Throwable) -> Unit
) {
    fun execute(isAuto: Boolean): Observable<GlucometerSyncResult> =
        syncWithGlucometer.execute(SyncWithGlucometerUseCase.Params())
            .concatWith(if (isAuto) Completable.complete() else syncGlucometers.execute())
            .doOnSubscribe { bus.event(Events.Sync.Glucometer.Started) }
            .doOnNext { result ->
                appMetric.trackEvent(AppMetricEvent.SnackProcessing)
                if (result.count > 0) {
                    appMetric.trackEvent(AppMetricEvent.ReceivedMeasurementsSugar)
                    bus.event(Events.EventsChanged(true))
                    if (result.hasInvalidTime) onInvalidTime()
                    bus.event(Events.Sync.Glucometer.Success)
                } else {
                    bus.event(Events.Sync.Glucometer.NoNewEvents)
                }
            }
            .doOnComplete { if (!isAuto) onServerSync() }
            .doOnError { error ->
                if (isAuto) onAutoError(error) else onManualError(error)
            }
            .onErrorResumeNext { error: Throwable -> recover(error, isAuto) }

    private fun recover(error: Throwable, isAuto: Boolean): ObservableSource<out GlucometerSyncResult> =
        when (error) {
            is BluetoothNotEnabledError -> {
                appMetric.trackEvent(AppMetricEvent.BluetoothTurningAlert)
                enableBluetoothAndRepeat(isAuto)
            }
            is LocationNotEnabledError -> enableLocationAndRepeat(isAuto)
            is LocationPermissionNotGrantedError -> {
                appMetric.trackEvent(AppMetricEvent.Permission.Alert.Location)
                requestLocationPermissionAndRepeat(isAuto)
            }
            is BluetoothPermissionNotGrantedError -> {
                appMetric.trackEvent(AppMetricEvent.Permission.Alert.Bluetooth)
                requestBluetoothPermissionAndRepeat(isAuto)
            }
            is GlucometerSyncError -> when (error.cause) {
                is BluetoothNotEnabledError -> {
                    appMetric.trackEvent(AppMetricEvent.BluetoothTurningAlert)
                    enableBluetoothAndRepeat(isAuto)
                }
                is LocationNotEnabledError -> enableLocationAndRepeat(isAuto)
                else -> Observable.error(error)
            }
            else -> Observable.error(error)
        }

    private fun requestLocationPermissionAndRepeat(isAuto: Boolean) =
        permissions.requestLocationPermission().filter { it }.flatMapObservable { execute(isAuto) }

    private fun requestBluetoothPermissionAndRepeat(isAuto: Boolean) =
        permissions.requestBluetoothPermission().filter { it }.flatMapObservable { execute(isAuto) }

    private fun enableLocationAndRepeat(isAuto: Boolean) =
        permissions.requestEnableLocation().filter { it }.flatMapObservable { execute(isAuto) }

    private fun enableBluetoothAndRepeat(isAuto: Boolean) =
        permissions.requestEnableBluetooth().filter { it }.flatMapObservable { execute(isAuto) }
}

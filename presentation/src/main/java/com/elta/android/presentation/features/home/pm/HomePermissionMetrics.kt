package com.elta.android.presentation.features.home.pm

import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.analytic.model.appmetric.params.AlertResultParam
import com.elta.android.presentation.analytic.model.appmetric.params.TurningResultParam
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

internal class HomePermissionMetrics(private val appMetric: AppMetricTracker) {

    fun bind(
        bluetoothPermissions: Observable<Boolean>,
        combinedPermissions: Observable<Boolean>,
        locationPermissions: Observable<Boolean>,
        bluetoothEnabled: Observable<Boolean>
    ): CompositeDisposable = CompositeDisposable(
        bluetoothPermissions.subscribe { granted ->
            appMetric.trackEvent(
                AppMetricEvent.Permission.AlertClick.Bluetooth(granted.toAlertResult())
            )
        },
        combinedPermissions.subscribe { granted ->
            val result = granted.toAlertResult()
            appMetric.trackEvent(AppMetricEvent.Permission.AlertClick.Bluetooth(result))
            appMetric.trackEvent(AppMetricEvent.Permission.AlertClick.Location(result))
        },
        locationPermissions.subscribe { granted ->
            appMetric.trackEvent(
                AppMetricEvent.Permission.AlertClick.Location(granted.toAlertResult())
            )
        },
        bluetoothEnabled.subscribe { enabled ->
            val result = if (enabled) TurningResultParam.ALLOW else TurningResultParam.REJECT
            appMetric.trackEvent(AppMetricEvent.BluetoothTurningAlertClick(result))
        }
    )

    private fun Boolean.toAlertResult() =
        if (this) AlertResultParam.ALLOW else AlertResultParam.PROHIBIT
}

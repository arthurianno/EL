package com.elta.android.presentation.features.home.pm

import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.model.appmetric.AppMetricAttribute
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.analytic.model.appmetric.params.AlertResultParam
import com.elta.android.presentation.analytic.model.appmetric.params.TurningResultParam
import io.reactivex.subjects.PublishSubject
import org.junit.Assert.assertEquals
import org.junit.Test

class HomePermissionMetricsTest {

    @Test
    fun `tracks each permission result and stops after disposal`() {
        val bluetoothPermissions = PublishSubject.create<Boolean>()
        val combinedPermissions = PublishSubject.create<Boolean>()
        val locationPermissions = PublishSubject.create<Boolean>()
        val bluetoothEnabled = PublishSubject.create<Boolean>()
        val events = mutableListOf<AppMetricEvent>()
        val tracker = object : AppMetricTracker {
            override fun trackEvent(event: AppMetricEvent) { events.add(event) }
            override fun setProfileAttributes(attributes: List<AppMetricAttribute>) = Unit
        }
        val subscription = HomePermissionMetrics(tracker).bind(
            bluetoothPermissions,
            combinedPermissions,
            locationPermissions,
            bluetoothEnabled
        )

        bluetoothPermissions.onNext(true)
        combinedPermissions.onNext(false)
        locationPermissions.onNext(true)
        bluetoothEnabled.onNext(false)

        assertEquals(
            listOf(
                AppMetricEvent.Permission.AlertClick.Bluetooth(AlertResultParam.ALLOW),
                AppMetricEvent.Permission.AlertClick.Bluetooth(AlertResultParam.PROHIBIT),
                AppMetricEvent.Permission.AlertClick.Location(AlertResultParam.PROHIBIT),
                AppMetricEvent.Permission.AlertClick.Location(AlertResultParam.ALLOW),
                AppMetricEvent.BluetoothTurningAlertClick(TurningResultParam.REJECT)
            ),
            events
        )

        subscription.dispose()
        bluetoothEnabled.onNext(true)
        assertEquals(5, events.size)
    }
}

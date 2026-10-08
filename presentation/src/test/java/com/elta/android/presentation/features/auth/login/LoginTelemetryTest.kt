package com.elta.android.presentation.features.auth.login

import com.elta.android.presentation.analytic.core.analytics.Analytics
import com.elta.android.presentation.analytic.core.analytics.AnalyticsTracker
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.encodeUserId
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEvent
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventParam
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventType
import com.elta.android.presentation.analytic.model.appmetric.AppMetricAttribute
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.di.AnalyticModule
import com.elta.android.presentation.features.auth.login.viewmodel.LoginTelemetry
import org.junit.Assert.assertEquals
import org.junit.Test

class LoginTelemetryTest {
    @Test
    fun `login events and account attributes keep their existing order`() {
        val events = mutableListOf<String>()
        var params: Map<String, String> = emptyMap()
        val tracker = object : AnalyticsTracker {
            override fun track(event: AnalyticsEvent) {
                events += event.name
            }

            override fun setStableParams(stableParams: Map<String, String>) {
                params = stableParams
            }
        }
        val appEvents = mutableListOf<AppMetricEvent>()
        val appMetric = object : AppMetricTracker {
            override fun trackEvent(event: AppMetricEvent) {
                appEvents += event
            }

            override fun setProfileAttributes(attributes: List<AppMetricAttribute>) = Unit
        }
        val telemetry = LoginTelemetry(
            Analytics(mapOf(AnalyticModule.FIREBASE to tracker)), appMetric
        )

        telemetry.loginClicked()
        telemetry.loginSucceeded()
        telemetry.accountIdentified("user@example.com")
        telemetry.passwordRecoveryOpened()

        assertEquals(listOf(AppMetricEvent.LoginClick), appEvents)
        assertEquals(listOf(AnalyticsEventType.LOG_IN, AnalyticsEventType.PASSWORD_RECOVERY), events)
        assertEquals(encodeUserId("user@example.com"), params[AnalyticsEventParam.ACCOUNT])
        assertEquals("false", params[AnalyticsEventParam.GENDER])
        assertEquals("false", params[AnalyticsEventParam.DIABETES])
    }
}

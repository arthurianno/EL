package com.elta.android.presentation.features.registration.main

import com.elta.android.presentation.analytic.core.analytics.Analytics
import com.elta.android.presentation.analytic.core.analytics.AnalyticsTracker
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEvent
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventType
import com.elta.android.presentation.analytic.model.appmetric.AppMetricAttribute
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.di.AnalyticModule
import com.elta.android.presentation.features.registration.main.viewmodel.RegistrationTelemetry
import org.junit.Assert.assertEquals
import org.junit.Test

class RegistrationTelemetryTest {
    @Test
    fun `policy click and successful registration send the existing events`() {
        val events = mutableListOf<String>()
        val tracker = object : AnalyticsTracker {
            override fun track(event: AnalyticsEvent) {
                events += event.name
            }

            override fun setStableParams(stableParams: Map<String, String>) = Unit
        }
        val appEvents = mutableListOf<AppMetricEvent>()
        val appMetric = object : AppMetricTracker {
            override fun trackEvent(event: AppMetricEvent) {
                appEvents += event
            }

            override fun setProfileAttributes(attributes: List<AppMetricAttribute>) = Unit
        }
        val telemetry = RegistrationTelemetry(
            Analytics(mapOf(AnalyticModule.FIREBASE to tracker)), appMetric
        )

        telemetry.privacyPolicyOpened()
        telemetry.registrationCompleted()

        assertEquals(listOf(AnalyticsEventType.TERMS_OF_USE), events)
        assertEquals(listOf(AppMetricEvent.RegistrationContinueClick), appEvents)
    }
}

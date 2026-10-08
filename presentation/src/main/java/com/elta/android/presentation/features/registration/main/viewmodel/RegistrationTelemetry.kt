package com.elta.android.presentation.features.registration.main.viewmodel

import com.elta.android.presentation.analytic.core.analytics.Analytics
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEvent
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventType
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import javax.inject.Inject

class RegistrationTelemetry @Inject constructor(
    private val analytics: Analytics,
    private val appMetric: AppMetricTracker
) {
    fun privacyPolicyOpened() {
        analytics.trackEvent(AnalyticsEvent(AnalyticsEventType.TERMS_OF_USE))
    }

    fun registrationCompleted() {
        appMetric.trackEvent(AppMetricEvent.RegistrationContinueClick)
    }
}

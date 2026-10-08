package com.elta.android.presentation.features.auth.login.viewmodel

import com.elta.android.presentation.analytic.core.analytics.Analytics
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.encodeUserId
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEvent
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventParam
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventType
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import javax.inject.Inject

class LoginTelemetry @Inject constructor(
    private val analytics: Analytics,
    private val appMetric: AppMetricTracker
) {
    fun loginClicked() {
        appMetric.trackEvent(AppMetricEvent.LoginClick)
    }

    fun loginSucceeded() {
        analytics.trackEvent(AnalyticsEvent(AnalyticsEventType.LOG_IN))
    }

    fun accountIdentified(id: String) {
        analytics.updateStableParams(
            mapOf(
                AnalyticsEventParam.ACCOUNT to encodeUserId(id),
                AnalyticsEventParam.GENDER to false.toString(),
                AnalyticsEventParam.DIABETES to false.toString()
            )
        )
    }

    fun passwordRecoveryOpened() {
        analytics.trackEvent(AnalyticsEvent(AnalyticsEventType.PASSWORD_RECOVERY))
    }
}

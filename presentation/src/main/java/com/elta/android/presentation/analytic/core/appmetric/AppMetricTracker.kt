package com.elta.android.presentation.analytic.core.appmetric

import com.elta.android.presentation.analytic.model.appmetric.AppMetricAttribute
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent

interface AppMetricTracker {
    fun trackEvent(event: AppMetricEvent)
    fun setProfileAttributes(attributes: List<AppMetricAttribute>)
}

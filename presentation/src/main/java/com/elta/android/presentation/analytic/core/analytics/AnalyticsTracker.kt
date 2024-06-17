package com.elta.android.presentation.analytic.core.analytics

import com.elta.android.presentation.analytic.model.analytics.AnalyticsEvent

interface AnalyticsTracker {
    fun setStableParams(stableParams: Map<String, String>)
    fun track(event: AnalyticsEvent)
}

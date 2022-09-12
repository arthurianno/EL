package com.elta.android.presentation.analytics.core

import com.elta.android.presentation.analytics.model.AnalyticsEvent

interface AnalyticsTracker {
    fun setStableParams(stableParams: Map<String, String>)
    fun track(event: AnalyticsEvent)
}

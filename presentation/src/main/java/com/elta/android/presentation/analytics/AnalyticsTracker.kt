package com.elta.android.presentation.analytics

import com.elta.android.presentation.analytics.AnalyticsEvent

interface AnalyticsTracker {
    fun setStableParams(stableParams: Map<String, String>)
    fun track(event: AnalyticsEvent)
}
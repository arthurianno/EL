package com.nullgr.android.presentation.analytics

import com.nullgr.android.presentation.analytics.AnalyticsEvent

interface AnalyticsTracker {
    fun setStableParams(stableParams: Map<String, String>)
    fun track(event: AnalyticsEvent)
}
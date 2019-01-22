package com.elta.android.presentation.analytics

interface AnalyticsTracker {
    fun setStableParams(stableParams: Map<String, String>)
    fun track(event: AnalyticsEvent)
}
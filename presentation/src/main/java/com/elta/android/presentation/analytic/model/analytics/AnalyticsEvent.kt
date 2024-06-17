package com.elta.android.presentation.analytic.model.analytics

data class AnalyticsEvent(
    @AnalyticsEventType val name: String,
    val params: Map<String, String> = hashMapOf()
)

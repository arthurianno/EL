package com.elta.android.presentation.analytics.model

data class AnalyticsEvent(
    @AnalyticsEventType val name: String,
    val params: Map<String, String> = hashMapOf()
)
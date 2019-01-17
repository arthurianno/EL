package com.elta.android.presentation.analytics

interface AnalyticsEvent {
    val name: String
    val params: Map<String, String>
        get() = hashMapOf()
}
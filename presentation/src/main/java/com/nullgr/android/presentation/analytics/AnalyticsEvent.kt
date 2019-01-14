package com.nullgr.android.presentation.analytics

interface AnalyticsEvent {
    val name: String
    val params: Map<String, String>
        get() = hashMapOf()
}
package com.elta.android.presentation.analytics.core

import com.elta.android.presentation.analytics.model.AnalyticsEvent
import com.elta.android.presentation.di.AnalyticsModule
import javax.inject.Inject

class Analytics @Inject constructor(
    private val trackers: Map<String, AnalyticsTracker>
) {

    private val defaultConfig = Config()
    private val stableParams = hashMapOf<String, String>()

    fun trackEvent(event: AnalyticsEvent, config: Config = defaultConfig) {
        trackAnalyticsEvent(event, config)
    }

    fun updateStableParams(key: String, value: String, config: Config = defaultConfig) {
        stableParams[key] = value
        setStableParams(stableParams, config)
    }

    fun updateStableParams(params: Map<String, String>, config: Config = defaultConfig) {
        params.forEach { stableParams[it.key] = it.value }
        setStableParams(stableParams, config)
    }

    fun updateStableParams(vararg params: Pair<String, String>, config: Config = defaultConfig) {
        params.forEach { stableParams[it.first] = it.second }
        setStableParams(stableParams, config)
    }

    private fun setStableParams(stableParams: Map<String, String>, config: Config = defaultConfig) {
        config.usedTrackers.forEach { name -> trackers[name]?.setStableParams(stableParams) }
    }

    private fun trackAnalyticsEvent(event: AnalyticsEvent, config: Config = defaultConfig) {
        config.usedTrackers.forEach { name -> trackers[name]?.track(event) }
    }

    data class Config(val usedTrackers: List<String> = listOf(AnalyticsModule.FIREBASE))
}
package com.elta.android.presentation.analytics.core

import android.content.Context
import android.os.Bundle
import com.elta.android.presentation.analytics.model.AnalyticsEvent
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject

class FirebaseTracker @Inject constructor(val context: Context) : AnalyticsTracker {

    private val firebaseAnalytics: FirebaseAnalytics
        get() = FirebaseAnalytics.getInstance(context)

    override fun track(event: AnalyticsEvent) {
        firebaseAnalytics.logEvent(event.name, Bundle().apply {
            event.params.forEach { param ->
                this.putString(param.key, param.value)
            }
        })
    }

    override fun setStableParams(stableParams: Map<String, String>) {
        stableParams.forEach {
            firebaseAnalytics.setUserProperty(it.key, it.value)
        }
    }
}
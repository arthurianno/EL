package com.elta.android.presentation.core.pm.listeners

import com.elta.android.presentation.analytics.model.AnalyticsEvent

interface Trackable {

    val analyticsEvent: AnalyticsEvent
}

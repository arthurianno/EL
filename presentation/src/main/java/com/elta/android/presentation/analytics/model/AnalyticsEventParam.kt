package com.elta.android.presentation.analytics.model

import android.support.annotation.StringDef

@StringDef(
    AnalyticsEventParam.LOG_TYPE,
    AnalyticsEventParam.GENDER,
    AnalyticsEventParam.TYPE,
    AnalyticsEventParam.SOURCE,
    AnalyticsEventParam.PERIOD,
    AnalyticsEventParam.SCREEN_NAME,
    AnalyticsEventParam.ACCOUNT,
    AnalyticsEventParam.DIABETES
)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class AnalyticsEventParam {
    companion object {
        const val LOG_TYPE = "log_type"
        const val GENDER = "gender"
        const val TYPE = "type"
        const val SOURCE = "source"
        const val PERIOD = "period"
        const val SCREEN_NAME = "screen_name"
        const val ACCOUNT = "account"
        const val DIABETES = "diabetes"
    }
}
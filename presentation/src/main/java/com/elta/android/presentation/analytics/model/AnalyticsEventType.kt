@file:Suppress("UtilityClassWithPublicConstructor")

package com.elta.android.presentation.analytics.model

import androidx.annotation.StringDef

@StringDef(
    AnalyticsEventType.APP_LAUNCH,
    AnalyticsEventType.LOG_IN,
    AnalyticsEventType.REGISTER_OPEN,
    AnalyticsEventType.TERMS_OF_USE,
    AnalyticsEventType.ONB_GENDER_ADD,
    AnalyticsEventType.ONB_DIABETES_ADD,
    AnalyticsEventType.ONB_WEIGHT_ADD,
    AnalyticsEventType.PASSWORD_RECOVERY,
    AnalyticsEventType.REMINDER_ADD,
    AnalyticsEventType.SHARE_GLUCOSE,
    AnalyticsEventType.HOMEPAGE,
    AnalyticsEventType.DIARY_OPEN,
    AnalyticsEventType.STATISTICS_OPEN,
    AnalyticsEventType.MAP_OPEN,
    AnalyticsEventType.PROFILE_OPEN,
    AnalyticsEventType.NEW_EVENT_OPEN,
    AnalyticsEventType.GLUCOMETER_ADD,
    AnalyticsEventType.GLUCOMETER_SYNCH,
    AnalyticsEventType.EVENT_BREAD_ADD,
    AnalyticsEventType.EVENT_INSULIN_ADD,
    AnalyticsEventType.EVENT_ACTIVITY_ADD,
    AnalyticsEventType.EVENT_WEIGHT_ADD,
    AnalyticsEventType.EVENT_MEDICAMENTS_ADD,
    AnalyticsEventType.GLYCATED_HEMOGLOBIN_ADD,
    AnalyticsEventType.PERIOD_TAB,
    AnalyticsEventType.APP_EXIT,
    AnalyticsEventType.GLUCOMETERS_OPEN
)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class AnalyticsEventType {
    companion object {
        const val APP_LAUNCH = "app_launch"
        const val LOG_IN = "log_in"
        const val REGISTER_OPEN = "register_open"
        const val TERMS_OF_USE = "terms_of_use"
        const val ONB_GENDER_ADD = "onb_gender_add"
        const val ONB_DIABETES_ADD = "onb_diabetes_add"
        const val ONB_WEIGHT_ADD = "onb_weight_add"
        const val PASSWORD_RECOVERY = "password_recovery"
        const val REMINDER_ADD = "reminder_add"
        const val SHARE_GLUCOSE = "share_glucose"
        const val HOMEPAGE = "homepage"
        const val DIARY_OPEN = "diary_open"
        const val STATISTICS_OPEN = "statistics_open"
        const val MAP_OPEN = "map_open"
        const val PROFILE_OPEN = "profile_open"
        const val NEW_EVENT_OPEN = "new_event_open"
        const val GLUCOMETER_ADD = "glucometer_add"
        const val GLUCOMETER_SYNCH = "glucometer_synch"
        const val EVENT_BREAD_ADD = "event_bread_add"
        const val EVENT_INSULIN_ADD = "event_insulin_add"
        const val EVENT_ACTIVITY_ADD = "event_activity_add"
        const val EVENT_WEIGHT_ADD = "event_weight_add"
        const val EVENT_MEDICAMENTS_ADD = "event_medicaments_add"
        const val GLYCATED_HEMOGLOBIN_ADD = "glycated_hemoglobin_add"
        const val PERIOD_TAB = "period_tab"
        const val APP_EXIT = "app_exit"
        const val GLUCOMETERS_OPEN = "glucometers_open"
    }
}
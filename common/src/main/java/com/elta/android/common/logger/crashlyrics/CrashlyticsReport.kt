package com.elta.android.common.logger.crashlyrics

interface CrashlyticsReport {

    fun log(message: String)

    fun setUserId(value: String)

    fun setCustomKey(key: String, value: String)

    fun writeException(exception: Throwable)
}

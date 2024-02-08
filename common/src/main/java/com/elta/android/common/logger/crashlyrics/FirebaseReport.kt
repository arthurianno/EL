package com.elta.android.common.logger.crashlyrics

import com.google.firebase.crashlytics.FirebaseCrashlytics

class FirebaseReport : CrashlyticsReport {

    private val firebaseCrashlytics: FirebaseCrashlytics
        get() = FirebaseCrashlytics.getInstance()

    override fun log(message: String) {
        firebaseCrashlytics.log(message)
    }

    override fun setUserId(value: String) {
        firebaseCrashlytics.setUserId(value)
    }

    override fun setCustomKey(key: String, value: String) {
        firebaseCrashlytics.setCustomKey(key, value)
    }

    override fun writeException(exception: Throwable) {
        firebaseCrashlytics.recordException(exception)
    }
}

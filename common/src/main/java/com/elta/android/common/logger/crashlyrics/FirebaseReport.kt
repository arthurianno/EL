package com.elta.android.common.logger.crashlyrics

import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class FirebaseReport : CrashlyticsReport {
    private val firebaseCrashlytics: FirebaseCrashlytics
        get() = FirebaseCrashlytics.getInstance()

    override fun log(message: String) {
        firebaseCrashlytics.log(message)
        Timber.tag(TAG).i(message)
    }

    override fun setUserId(value: String) {
        firebaseCrashlytics.setUserId(value)
    }

    override fun setCustomKey(key: String, value: String) {
        firebaseCrashlytics.setCustomKey(key, value)
    }

    override fun writeException(exception: Throwable) {
        firebaseCrashlytics.recordException(exception)
        Timber.tag(TAG).e(exception)
    }

    override fun enableCrashlytics() {
        firebaseCrashlytics.setCrashlyticsCollectionEnabled(true)
    }


}

private const val TAG = "LocalReports"
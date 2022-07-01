package com.elta.android.data.core.network

import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber

class TimberInterceptorLogger(
    private val tag: String
) : HttpLoggingInterceptor.Logger {

    override fun log(message: String) = Timber.tag(tag).d(message)
}

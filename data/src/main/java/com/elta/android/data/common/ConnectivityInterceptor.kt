package com.elta.android.data.common

import com.elta.android.common.errors.NetworkConnectionError
import com.nullgr.core.hardware.NetworkChecker
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class ConnectivityInterceptor @Inject constructor(
    private val checker: NetworkChecker
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response =
        if (checker.isInternetConnectionEnabled()) chain.proceed(chain.request())
        else throw NetworkConnectionError()
}

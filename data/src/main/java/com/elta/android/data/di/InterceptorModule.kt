package com.elta.android.data.di

import com.elta.android.data.core.network.TimberInterceptorLogger
import com.elta.android.data.core.qualifires.Interceptors
import com.elta.android.data.core.qualifires.NetworkInterceptors
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

@Module
class InterceptorModule(
    private val tag: String,
    private val level: HttpLoggingInterceptor.Level
) {

    @Provides
    @Singleton
    fun logger(): HttpLoggingInterceptor.Logger = TimberInterceptorLogger(tag)

    @Provides
    @Singleton
    fun httpLoggingInterceptor(logger: HttpLoggingInterceptor.Logger): HttpLoggingInterceptor =
        HttpLoggingInterceptor(logger).setLevel(level)

    @Provides
    @Singleton
    @Interceptors
    fun interceptors(
        httpLoggingInterceptor: HttpLoggingInterceptor
    ): List<@JvmWildcard Interceptor> =
        arrayListOf<Interceptor>().apply {
            add(httpLoggingInterceptor)
        }

    @Provides
    @Singleton
    @NetworkInterceptors
    fun networkInterceptors(): List<@JvmWildcard Interceptor> = emptyList()
}
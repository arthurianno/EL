package com.nullgr.android.data.di

import android.content.Context
import com.google.gson.Gson
import com.nullgr.android.data.core.network.GsonFactory
import com.nullgr.android.data.core.network.OkHttpClientFactory
import com.nullgr.android.data.core.network.RetrofitFactory
import com.nullgr.android.data.core.qualifires.Interceptors
import com.nullgr.android.data.core.qualifires.NetworkInterceptors
import com.nullgr.android.data.core.qualifires.ServerUrl
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import javax.inject.Singleton

@Module(includes = [InterceptorModule::class])
class NetworkModule {

    @Provides
    fun cacheFolder(context: Context): File = context.cacheDir

    @Provides
    @Singleton
    fun callAdapterFactory(): CallAdapter.Factory = RxJava2CallAdapterFactory.create()

    @Provides
    @Singleton
    fun gson(): Gson = GsonFactory.create()

    @Provides
    @Singleton
    fun converterFactory(gson: Gson): Converter.Factory = GsonConverterFactory.create(gson)

    @Provides
    fun okHttpClient(
        cacheFolder: File,
        @Interceptors interceptors: List<Interceptor>,
        @NetworkInterceptors networkInterceptors: List<Interceptor>
    ): OkHttpClient = OkHttpClientFactory.create(cacheFolder, interceptors, networkInterceptors)

    @Provides
    @Singleton
    fun retrofit(
        okHttpClient: OkHttpClient,
        callAdapterFactory: CallAdapter.Factory,
        converterFactory: Converter.Factory,
        @ServerUrl baseUrl: String
    ): Retrofit = RetrofitFactory.create(okHttpClient, callAdapterFactory, converterFactory, baseUrl)
}
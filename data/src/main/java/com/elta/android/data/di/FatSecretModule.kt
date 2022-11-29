package com.elta.android.data.di

import com.elta.android.common.di.qualifires.FatSecret
import com.elta.android.common.di.qualifires.FeatSecretAnnotationType
import com.elta.android.data.common.ConnectivityInterceptor
import com.elta.android.data.common.FatSecretErrorInterceptor
import com.elta.android.data.common.FatSecretInterceptor
import com.elta.android.data.core.network.OkHttpClientFactory
import com.elta.android.data.core.network.RetrofitFactory
import com.elta.android.data.features.calculator.storage.FatSecretDataStorage
import com.elta.android.data.features.calculator.storage.FatSecretStorage
import com.nullgr.core.security.prefs.CryptoPreferences
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.CallAdapter
import retrofit2.Converter.Factory
import retrofit2.Retrofit
import java.io.File
import javax.inject.Singleton

@Module
class FatSecretModule {
    private val clientId = "6893a669c38f4b3c97154f721f405c9f"
    private val clientSecret = "f8bce065b49b42c89dc0a8af8041b790"
    private val consumerKey = "6893a669c38f4b3c97154f721f405c9f"
    private val consumerSecret = "b0876f15ad364783a1c0d721f80456e9"
    private val baseUrl = "https://platform.fatsecret.com/rest/server.api/"
    private val tokenUrl = "https://oauth.fatsecret.com/connect/"

    @Provides
    @FatSecret(FeatSecretAnnotationType.ClientId)
    fun provideFatSecretClientId(): String = clientId

    @Provides
    @FatSecret(FeatSecretAnnotationType.ClientSecret)
    fun provideFatSecretClientSecret(): String = clientSecret

    @Provides
    @FatSecret(FeatSecretAnnotationType.BaseUrl)
    fun provideFatSecretBaseUrl(): String = baseUrl

    @Provides
    @FatSecret(FeatSecretAnnotationType.TokenUrl)
    fun provideFatSecretTokenUrl(): String = tokenUrl

    @Provides
    @Singleton
    fun provideFatSecretStorage(preferences: CryptoPreferences): FatSecretStorage =
        FatSecretDataStorage(preferences)

    @Provides
    @FatSecret(FeatSecretAnnotationType.OkHttpClient)
    fun provideFatSecretOkHttpClient(
        cacheFolder: File,
        @FatSecret(FeatSecretAnnotationType.Interceptors) interceptors: List<Interceptor>,
        @FatSecret(FeatSecretAnnotationType.NetworkInterceptors) networkInterceptors: List<Interceptor>
    ): OkHttpClient = OkHttpClientFactory.create(
        cacheFolder,
        interceptors,
        networkInterceptors
    )

    @Provides
    @Singleton
    @FatSecret(FeatSecretAnnotationType.Token)
    fun provideTokenOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        @FatSecret(FeatSecretAnnotationType.NetworkInterceptors) networkInterceptors: List<Interceptor>
    ): OkHttpClient = OkHttpClientFactory.create(listOf(loggingInterceptor), networkInterceptors)

    @Provides
    @Singleton
    @FatSecret(FeatSecretAnnotationType.Retrofit)
    fun provideFatSecretRetrofit(
        @FatSecret(FeatSecretAnnotationType.OkHttpClient) okHttpClient: OkHttpClient,
        callAdapterFactory: CallAdapter.Factory,
        converterFactory: Factory,
        @FatSecret(FeatSecretAnnotationType.BaseUrl) baseUrl: String
    ): Retrofit =
        RetrofitFactory.create(okHttpClient, callAdapterFactory, converterFactory, baseUrl)

    @Provides
    @Singleton
    @FatSecret(FeatSecretAnnotationType.Token)
    fun provideTokenRetrofit(
        @FatSecret(FeatSecretAnnotationType.Token) okHttpClient: OkHttpClient,
        callAdapterFactory: CallAdapter.Factory,
        converterFactory: Factory,
        @FatSecret(FeatSecretAnnotationType.TokenUrl) baseUrl: String
    ): Retrofit =
        RetrofitFactory.create(
            okHttpClient = okHttpClient,
            callAdapterFactory = callAdapterFactory,
            converterFactory = converterFactory,
            url = baseUrl
        )

    @Provides
    @Singleton
    @FatSecret(FeatSecretAnnotationType.NetworkInterceptors)
    fun provideFatSecretNetworkInterceptors(
        errorInterceptor: FatSecretErrorInterceptor
    ): List<@JvmWildcard Interceptor> = listOf(
        errorInterceptor
    )

    @Provides
    @Singleton
    @FatSecret(FeatSecretAnnotationType.Interceptors)
    fun provideFatSecretInterceptors(
        connectivityInterceptor: ConnectivityInterceptor,
        httpLoggingInterceptor: HttpLoggingInterceptor,
        interceptor: FatSecretInterceptor
    ): List<@JvmWildcard Interceptor> = listOf(
        connectivityInterceptor,
        httpLoggingInterceptor,
        interceptor
    )
}

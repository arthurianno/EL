package com.elta.android.data.di

import com.elta.android.common.di.qualifires.FatSecret
import com.elta.android.common.di.qualifires.FatSecretAnnotationType
import com.elta.android.data.common.ConnectivityInterceptor
import com.elta.android.data.common.FatSecretErrorInterceptor
import com.elta.android.data.common.FatSecretOAuth2Interceptor
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

private const val CLIENT_ID = "6893a669c38f4b3c97154f721f405c9f"
private const val CLIENT_SECRET = "f8bce065b49b42c89dc0a8af8041b790"
private const val CONSUMER_KEY = "a55d2498c3964d3c9414a60456f56fce"
private const val CONSUMER_SECRET = "955e724c2aca4a5485cf3c20661ef8c1"
private const val BASE_URL = "https://platform.fatsecret.com/rest/"
private const val TOKEN_URL = "https://oauth.fatsecret.com/connect/"
private const val USE_OAUTH2 = false

@Module
class FatSecretModule {

    @Provides
    @FatSecret(FatSecretAnnotationType.ClientId)
    fun provideFatSecretClientId(): String = CLIENT_ID

    @Provides
    @FatSecret(FatSecretAnnotationType.ClientSecret)
    fun provideFatSecretClientSecret(): String = CLIENT_SECRET

    @Provides
    @FatSecret(FatSecretAnnotationType.ConsumerKey)
    fun provideFatSecretConsumerKey(): String = CONSUMER_KEY

    @Provides
    @FatSecret(FatSecretAnnotationType.ConsumerSecret)
    fun provideFatSecretConsumerSecret(): String = CONSUMER_SECRET

    @Provides
    @FatSecret(FatSecretAnnotationType.BaseUrl)
    fun provideFatSecretBaseUrl(): String = BASE_URL

    @Provides
    @FatSecret(FatSecretAnnotationType.TokenUrl)
    fun provideFatSecretTokenUrl(): String = TOKEN_URL

    @Provides
    @FatSecret(FatSecretAnnotationType.IsOAuth2)
    fun provideFatSecretIsOAuth2(): Boolean = USE_OAUTH2

    @Provides
    @Singleton
    fun provideFatSecretStorage(preferences: CryptoPreferences): FatSecretStorage =
        FatSecretDataStorage(preferences)

    @Provides
    @FatSecret(FatSecretAnnotationType.OkHttpClient)
    fun provideFatSecretOkHttpClient(
        cacheFolder: File,
        @FatSecret(FatSecretAnnotationType.Interceptors) interceptors: List<Interceptor>,
        @FatSecret(FatSecretAnnotationType.NetworkInterceptors) networkInterceptors: List<Interceptor>
    ): OkHttpClient = OkHttpClientFactory.create(
        cacheFolder,
        interceptors,
        networkInterceptors
    )

    @Provides
    @Singleton
    @FatSecret(FatSecretAnnotationType.Token)
    fun provideTokenOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        @FatSecret(FatSecretAnnotationType.NetworkInterceptors) networkInterceptors: List<Interceptor>
    ): OkHttpClient = OkHttpClientFactory.create(listOf(loggingInterceptor), networkInterceptors)

    @Provides
    @Singleton
    @FatSecret(FatSecretAnnotationType.Retrofit)
    fun provideFatSecretRetrofit(
        @FatSecret(FatSecretAnnotationType.OkHttpClient) okHttpClient: OkHttpClient,
        callAdapterFactory: CallAdapter.Factory,
        converterFactory: Factory,
        @FatSecret(FatSecretAnnotationType.BaseUrl) baseUrl: String
    ): Retrofit =
        RetrofitFactory.create(okHttpClient, callAdapterFactory, converterFactory, baseUrl)

    @Provides
    @Singleton
    @FatSecret(FatSecretAnnotationType.Token)
    fun provideTokenRetrofit(
        @FatSecret(FatSecretAnnotationType.Token) okHttpClient: OkHttpClient,
        callAdapterFactory: CallAdapter.Factory,
        converterFactory: Factory,
        @FatSecret(FatSecretAnnotationType.TokenUrl) baseUrl: String
    ): Retrofit =
        RetrofitFactory.create(
            okHttpClient = okHttpClient,
            callAdapterFactory = callAdapterFactory,
            converterFactory = converterFactory,
            url = baseUrl
        )

    @Provides
    @Singleton
    @FatSecret(FatSecretAnnotationType.NetworkInterceptors)
    fun provideFatSecretNetworkInterceptors(
        errorInterceptor: FatSecretErrorInterceptor
    ): List<@JvmWildcard Interceptor> = listOf(
        errorInterceptor
    )

    @Provides
    @Singleton
    @FatSecret(FatSecretAnnotationType.Interceptors)
    fun provideFatSecretInterceptors(
        connectivityInterceptor: ConnectivityInterceptor,
        httpLoggingInterceptor: HttpLoggingInterceptor,
        oAuth2Interceptor: FatSecretOAuth2Interceptor
    ): List<@JvmWildcard Interceptor> = listOfNotNull(
        connectivityInterceptor,
        httpLoggingInterceptor,
        oAuth2Interceptor.takeIf { USE_OAUTH2 }
    )
}

package com.elta.android.data.di

import com.elta.android.common.di.qualifires.Token
import com.elta.android.data.common.TokenAuthenticator
import com.elta.android.data.core.network.OkHttpClientFactory
import com.elta.android.data.core.network.RetrofitFactory
import com.elta.android.data.core.qualifires.ServerUrl
import com.elta.android.data.features.auth.api.TokenRefreshApi
import com.elta.android.data.features.auth.storage.LocalTokenStorage
import com.elta.android.data.features.auth.storage.TokenStorage
import com.nullgr.core.security.prefs.CryptoPreferences
import dagger.Module
import dagger.Provides
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
class TokenModule {

    @Provides
    @Singleton
    fun tokenStorage(pref: CryptoPreferences, api: TokenRefreshApi): TokenStorage = LocalTokenStorage(pref, api)

    @Provides
    @Singleton
    fun authenticator(storage: TokenStorage): Authenticator = TokenAuthenticator(storage)

    @Token
    @Provides
    @Singleton
    fun tokenOkHttpClient(): OkHttpClient = OkHttpClientFactory.create()

    @Token
    @Provides
    @Singleton
    fun tokenRetrofit(
        @Token okHttpClient: OkHttpClient,
        callAdapterFactory: CallAdapter.Factory,
        converterFactory: Converter.Factory,
        @ServerUrl baseUrl: String
    ): Retrofit = RetrofitFactory.create(
        okHttpClient,
        callAdapterFactory,
        converterFactory,
        baseUrl,
        Executors.newSingleThreadExecutor()
    )
}
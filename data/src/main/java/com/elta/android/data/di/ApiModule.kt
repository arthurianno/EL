package com.elta.android.data.di

import com.elta.android.data.features.auth.api.AuthApi
import com.elta.android.data.features.auth.api.SocialApi
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@Suppress("FunctionOnlyReturningConstant", "TooManyFunctions")
class ApiModule {

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create<AuthApi>(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideAuthSocialApi(retrofit: Retrofit): SocialApi = retrofit.create<SocialApi>(SocialApi::class.java)

    object ApiConfig
}
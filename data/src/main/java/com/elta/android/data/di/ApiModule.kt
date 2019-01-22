package com.elta.android.data.di

import com.elta.android.data.features.auth.api.AuthApi
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

    object ApiConfig
}
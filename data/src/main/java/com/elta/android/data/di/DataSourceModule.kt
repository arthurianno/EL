package com.elta.android.data.di

import com.elta.android.data.features.auth.datasource.AuthDataSource
import com.elta.android.data.features.auth.datasource.AuthRemoteDataSource
import com.elta.android.data.features.auth.datasource.AuthSocialDataSource
import com.elta.android.data.features.auth.datasource.AuthSocialRemoteDataSource
import com.elta.android.data.features.user.datasource.SettingsDataSource
import com.elta.android.data.features.user.datasource.SettingsRemoteDataSource
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Suppress("TooManyFunctions")
@Module
abstract class DataSourceModule {

    @Binds
    @Singleton
    abstract fun bindAuthDataSource(source: AuthRemoteDataSource): AuthDataSource

    @Binds
    @Singleton
    abstract fun bindAuthSocialDataSource(source: AuthSocialRemoteDataSource): AuthSocialDataSource

    @Binds
    @Singleton
    abstract fun bindSettingsDataSource(source: SettingsRemoteDataSource): SettingsDataSource
}
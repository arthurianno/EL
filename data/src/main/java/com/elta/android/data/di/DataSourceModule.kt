package com.elta.android.data.di

import com.elta.android.data.features.auth.datasource.AuthDataSource
import com.elta.android.data.features.auth.datasource.AuthRemoteDataSource
import com.elta.android.data.features.auth.datasource.social.SocialNetworkSdkDataSource
import com.elta.android.data.features.auth.datasource.social.SocialNetworkDataSource
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
    abstract fun bindSocialNetworkTokenDataSource(source: SocialNetworkSdkDataSource): SocialNetworkDataSource
}
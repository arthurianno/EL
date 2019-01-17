package com.elta.android.data.di

import com.elta.android.data.features.feature1.datasource.TestDataSource
import com.elta.android.data.features.feature1.datasource.TestRemoteDataSource
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Suppress("TooManyFunctions")
@Module
abstract class DataSourceModule {

    @Binds
    @Singleton
    abstract fun bindTestDataSource(source: TestRemoteDataSource): TestDataSource
}
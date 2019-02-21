package com.elta.android.data.di

import com.elta.android.common.di.qualifires.Cache
import com.elta.android.common.di.qualifires.Remote
import com.elta.android.data.features.auth.datasource.AuthDataSource
import com.elta.android.data.features.auth.datasource.AuthRemoteDataSource
import com.elta.android.data.features.auth.datasource.AuthSocialDataSource
import com.elta.android.data.features.auth.datasource.AuthSocialRemoteDataSource
import com.elta.android.data.features.diary.events.datasource.EventsCachedDataSource
import com.elta.android.data.features.diary.events.datasource.EventsDataSource
import com.elta.android.data.features.diary.events.datasource.EventsRemoteDataSource
import com.elta.android.data.features.diary.tags.datasource.TagsCachedDataSource
import com.elta.android.data.features.diary.tags.datasource.TagsDataSource
import com.elta.android.data.features.diary.tags.datasource.TagsRemoteDataSource
import com.elta.android.data.features.sale_points.datasource.SalePointsCachedDataSource
import com.elta.android.data.features.sale_points.datasource.SalePointsDataSource
import com.elta.android.data.features.sale_points.datasource.SalePointsRemoteDataSource
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

    @Remote
    @Binds
    @Singleton
    abstract fun bindSalePointsRemoteDataSource(source: SalePointsRemoteDataSource): SalePointsDataSource

    @Cache
    @Binds
    @Singleton
    abstract fun bindSalePointsCacheDataSource(source: SalePointsCachedDataSource): SalePointsDataSource

    @Remote
    @Binds
    @Singleton
    abstract fun bindEventsRemoteDataSource(source: EventsRemoteDataSource): EventsDataSource

    @Cache
    @Binds
    @Singleton
    abstract fun bindEventsCachedDataSource(source: EventsCachedDataSource): EventsDataSource

    @Remote
    @Binds
    @Singleton
    abstract fun bindTagsRemoteDataSource(source: TagsRemoteDataSource): TagsDataSource

    @Cache
    @Binds
    @Singleton
    abstract fun bindTagsCachedDataSource(source: TagsCachedDataSource): TagsDataSource
}
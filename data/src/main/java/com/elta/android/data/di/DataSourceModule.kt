package com.elta.android.data.di

import com.elta.android.common.di.qualifires.Cache
import com.elta.android.common.di.qualifires.Remote
import com.elta.android.data.features.auth.datasource.AuthDataSource
import com.elta.android.data.features.auth.datasource.AuthRemoteDataSource
import com.elta.android.data.features.auth.datasource.AuthSocialDataSource
import com.elta.android.data.features.auth.datasource.AuthSocialRemoteDataSource
import com.elta.android.data.features.devices.datasource.DeviceDataSource
import com.elta.android.data.features.devices.datasource.DeviceRemoteDataSource
import com.elta.android.data.features.diary.events.datasource.EventsCachedDataSource
import com.elta.android.data.features.diary.events.datasource.EventsDataSource
import com.elta.android.data.features.diary.events.datasource.EventsRemoteDataSource
import com.elta.android.data.features.diary.insulin.datasource.InsulinNameDataSource
import com.elta.android.data.features.diary.insulin.datasource.InsulinNameRemoteDataSource
import com.elta.android.data.features.diary.tags.datasource.TagsCachedDataSource
import com.elta.android.data.features.diary.tags.datasource.TagsDataSource
import com.elta.android.data.features.diary.tags.datasource.TagsRemoteDataSource
import com.elta.android.data.features.feedback.datasource.FeedbackDataSource
import com.elta.android.data.features.feedback.datasource.FeedbackRemoteDataSource
import com.elta.android.data.features.firmware.datasource.FirmwareDataSource
import com.elta.android.data.features.firmware.datasource.FirmwareLocalDataSource
import com.elta.android.data.features.firmware.datasource.FirmwareRemoteDataSource
import com.elta.android.data.features.googlefit.datasource.GoogleFitDataSource
import com.elta.android.data.features.googlefit.datasource.HealthAppDataSource
import com.elta.android.data.features.observers.datasource.ObserverCachedDataSource
import com.elta.android.data.features.observers.datasource.ObserverDataSource
import com.elta.android.data.features.observers.datasource.ObserverRemoteDataSource
import com.elta.android.data.features.reminder.datasource.RemindersCacheDataSource
import com.elta.android.data.features.reminder.datasource.RemindersDataSource
import com.elta.android.data.features.reports.datasource.ReportsDataSource
import com.elta.android.data.features.reports.datasource.ReportsRemoteDataSource
import com.elta.android.data.features.sale_points.datasource.SalePointsCachedDataSource
import com.elta.android.data.features.sale_points.datasource.SalePointsDataSource
import com.elta.android.data.features.sale_points.datasource.SalePointsRemoteDataSource
import com.elta.android.data.features.sync.datasource.LocalSyncCachedDataSource
import com.elta.android.data.features.sync.datasource.LocalSyncDataSource
import com.elta.android.data.features.user.datasource.ProfileCachedDataSource
import com.elta.android.data.features.user.datasource.ProfileDataSource
import com.elta.android.data.features.user.datasource.ProfileRemoteDataSource
import com.elta.android.data.features.userinfo.datasource.UserInfoCachedDataSource
import com.elta.android.data.features.userinfo.datasource.UserInfoDataSource
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

    @Remote
    @Binds
    @Singleton
    abstract fun bindProfileRemoteDataSource(source: ProfileRemoteDataSource): ProfileDataSource

    @Cache
    @Binds
    @Singleton
    abstract fun bindProfileCachedDataSource(source: ProfileCachedDataSource): ProfileDataSource

    @Remote
    @Binds
    @Singleton
    abstract fun bindObserverRemoteDataSource(source: ObserverRemoteDataSource): ObserverDataSource

    @Cache
    @Binds
    @Singleton
    abstract fun bindObserverCachedDataSource(source: ObserverCachedDataSource): ObserverDataSource

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

    @Remote
    @Binds
    @Singleton
    abstract fun bindInsulinNameRemoteDataSource(source: InsulinNameRemoteDataSource): InsulinNameDataSource

    @Binds
    @Singleton
    abstract fun bindDeviceDataSource(source: DeviceRemoteDataSource): DeviceDataSource

    @Cache
    @Binds
    @Singleton
    abstract fun bindRemindersCacheDataSource(source: RemindersCacheDataSource): RemindersDataSource

    @Remote
    @Binds
    @Singleton
    abstract fun bindFirmwareRemoteDataSource(source: FirmwareRemoteDataSource): FirmwareDataSource

    @Cache
    @Binds
    @Singleton
    abstract fun bindFirmwareLocalDataSource(source: FirmwareLocalDataSource): FirmwareDataSource

    @Binds
    @Singleton
    abstract fun bindFeedbackRemoteDataSource(source: FeedbackRemoteDataSource): FeedbackDataSource

    @Binds
    @Singleton
    abstract fun bindSyncChangesCacheDataSource(source: LocalSyncCachedDataSource): LocalSyncDataSource

    @Binds
    @Singleton
    abstract fun bindUserInfoDataSource(sourceInfo: UserInfoCachedDataSource): UserInfoDataSource

    @Binds
    @Singleton
    abstract fun bindHealthAppDataSource(source: GoogleFitDataSource): HealthAppDataSource

    @Binds
    @Singleton
    abstract fun bindReportsDataSources(source: ReportsRemoteDataSource): ReportsDataSource
}

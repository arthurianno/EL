package com.elta.android.data.di

import com.elta.android.data.features.calculator.cache.DishCache
import com.elta.android.data.features.calculator.cache.SearchHistoryCache
import com.elta.android.data.features.calculator.cache.VerifiedProductCache
import com.elta.android.data.features.calculator.cache.model.DishDbEntity
import com.elta.android.data.features.calculator.cache.model.SearchHistoryDbEntity
import com.elta.android.data.features.calculator.cache.model.VerifiedProductDbEntity
import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.devices.cache.DbGlucometersCache
import com.elta.android.data.features.devices.cache.DbGlucometersInfoCache
import com.elta.android.data.features.devices.cache.dto.GlucometerCachedDto
import com.elta.android.data.features.devices.cache.dto.GlucometerInfoCachedDto
import com.elta.android.data.features.diary.events.cache.DbEventsCache
import com.elta.android.data.features.diary.events.cache.dto.EventCachedDto
import com.elta.android.data.features.diary.insulin.cache.DbDrugsCache
import com.elta.android.data.features.diary.insulin.cache.DrugCachedDto
import com.elta.android.data.features.diary.tags.cache.DbTagsCache
import com.elta.android.data.features.diary.tags.cache.dto.TagCachedDto
import com.elta.android.data.features.observers.cache.DbObserverCache
import com.elta.android.data.features.observers.model.ObserverDbEntity
import com.elta.android.data.features.reminder.cache.DbReminderCache
import com.elta.android.data.features.reminder.cache.dto.ReminderCacheDto
import com.elta.android.data.features.sale_points.cache.DbSalePointsCache
import com.elta.android.data.features.sale_points.cache.dto.SalePointCacheDto
import com.elta.android.data.features.sync.cache.LocalSyncChangesCache
import com.elta.android.data.features.sync.cache.dto.LocalSyncCachedDto
import com.elta.android.data.features.user.cache.DbProfileCache
import com.elta.android.data.features.user.cache.ProfileSettingsCache
import com.elta.android.data.features.user.cache.dto.ProfileCacheDto
import com.elta.android.data.features.user.cache.dto.ProfileSettingsDbEntity
import com.elta.android.data.features.userinfo.cache.DbUserInfoCache
import com.elta.android.data.features.userinfo.cache.dto.UserInfoDbEntity
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Suppress("TooManyFunctions", "ComplexInterface")
@Module(includes = [CacheModule.Declarations::class])
class CacheModule {

    @Module
    interface Declarations {
        @Binds
        @Singleton
        fun bindSalePointsCache(cache: DbSalePointsCache): Cache<SalePointCacheDto>

        @Binds
        @Singleton
        fun bindEventsCache(cache: DbEventsCache): Cache<EventCachedDto>

        @Binds
        @Singleton
        fun bindTagsCache(cache: DbTagsCache): Cache<TagCachedDto>

        @Binds
        @Singleton
        fun bindDrugsCache(cache: DbDrugsCache): Cache<DrugCachedDto>

        @Binds
        @Singleton
        fun bindObserverCache(cache: DbObserverCache): Cache<ObserverDbEntity>

        @Binds
        @Singleton
        fun bindProfileCache(cache: DbProfileCache): Cache<ProfileCacheDto>

        @Binds
        @Singleton
        fun bindProfileSettingsCache(cache: ProfileSettingsCache): Cache<ProfileSettingsDbEntity>

        @Binds
        @Singleton
        fun bindReminderCache(cache: DbReminderCache): Cache<ReminderCacheDto>

        @Binds
        @Singleton
        fun bindGlucometersCache(cache: DbGlucometersCache): Cache<GlucometerCachedDto>

        @Binds
        @Singleton
        fun bindGlucometersInfoCache(cache: DbGlucometersInfoCache): Cache<GlucometerInfoCachedDto>

        @Binds
        @Singleton
        fun bindSyncChangesCache(cached: LocalSyncChangesCache): Cache<LocalSyncCachedDto>

        @Binds
        @Singleton
        fun bindUserInfoCache(cached: DbUserInfoCache): Cache<UserInfoDbEntity>

        @Binds
        @Singleton
        fun bindSearchHistoryCache(cached: SearchHistoryCache): Cache<SearchHistoryDbEntity>

        @Binds
        @Singleton
        fun bindDishCache(cached: DishCache): Cache<DishDbEntity>

        @Binds
        @Singleton
        fun bingVerifiedProductCache(cached: VerifiedProductCache): Cache<VerifiedProductDbEntity>
    }
}

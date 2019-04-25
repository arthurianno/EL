package com.elta.android.data.di

import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.diary.events.cache.DbEventsCache
import com.elta.android.data.features.diary.events.cache.dto.EventCachedDto
import com.elta.android.data.features.diary.tags.cache.DbTagsCache
import com.elta.android.data.features.diary.tags.cache.dto.TagCachedDto
import com.elta.android.data.features.observers.cache.DbObserverCache
import com.elta.android.data.features.observers.cache.dto.ObserverCacheDto
import com.elta.android.data.features.reminder.cache.DbReminderCache
import com.elta.android.data.features.reminder.cache.dto.ReminderCacheDto
import com.elta.android.data.features.sale_points.cache.DbSalePointsCache
import com.elta.android.data.features.sale_points.cache.dto.SalePointCacheDto
import com.elta.android.data.features.user.cache.DbProfileCache
import com.elta.android.data.features.user.cache.dto.ProfileCacheDto
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

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
        fun bindObserverCache(cache: DbObserverCache): Cache<ObserverCacheDto>

        @Binds
        @Singleton
        fun bindProfileCache(cache: DbProfileCache): Cache<ProfileCacheDto>

        @Binds
        @Singleton
        fun bindReminderCache(cache: DbReminderCache): Cache<ReminderCacheDto>
    }
}
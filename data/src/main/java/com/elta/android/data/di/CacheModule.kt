package com.elta.android.data.di

import android.content.Context
import com.elta.android.data.features.diary.MyObjectBox
import com.elta.android.data.features.diary.events.cache.DbEventsCache
import com.elta.android.data.features.diary.events.cache.EventsCache
import com.elta.android.data.features.diary.tags.cache.DbTagsCache
import com.elta.android.data.features.diary.tags.cache.TagsCache
import com.elta.android.data.features.sale_points.cache.DbSalePointsCache
import com.elta.android.data.features.sale_points.cache.SalePointsCache
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.objectbox.BoxStore
import javax.inject.Singleton

@Module(includes = [CacheModule.Declarations::class])
class CacheModule {

    @Module
    interface Declarations {
        @Binds
        @Singleton
        fun bindSalePointsCache(cache: DbSalePointsCache): SalePointsCache

        @Binds
        @Singleton
        fun bindEventsCache(cache: DbEventsCache): EventsCache

        @Binds
        @Singleton
        fun bindTagsCache(cache: DbTagsCache): TagsCache
    }

    @Provides
    @Singleton
    fun provideBoxStore(context: Context): BoxStore = MyObjectBox.builder().androidContext(context).build()
}